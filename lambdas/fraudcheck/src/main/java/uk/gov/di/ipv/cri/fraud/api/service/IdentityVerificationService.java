package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.audit.TPREFraudAuditExtension;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.IDENTITY_THEFT_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.IMPERSONATION_RISK_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.MORTALITY_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.SYNTHETIC_IDENTITY_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.FRAUD_CHECK_CI_PREFIX;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.FRAUD_CHECK_REQUEST_FAILED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.FRAUD_CHECK_REQUEST_SUCCEEDED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.IDENTITY_CHECK_SCORE_PREFIX;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PEP_CHECK_CI_PREFIX;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PEP_CHECK_REQUEST_FAILED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PEP_CHECK_REQUEST_SUCCEEDED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PERSON_DETAILS_VALIDATION_FAIL;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PERSON_DETAILS_VALIDATION_PASS;

public class IdentityVerificationService {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ERROR_MSG_CONTEXT =
            "Error occurred when attempting to invoke the third party api";
    private static final String ERROR_FRAUD_CHECK_RESULT_RETURN_NULL =
            "Null FraudCheckResult returned when invoking third party API.";
    private static final String ERROR_FRAUD_CHECK_RESULT_NO_ERR_MSG =
            "FraudCheckResult had no error message.";
    private final ThirdPartyFraudGateway thirdPartyGateway;
    private final PersonIdentityValidator personIdentityValidator;
    private final ContraindicationMapper contraindicationMapper;
    private final IdentityScoreCalculator identityScoreCalculator;
    private final ConfigurationService configurationService;
    private final AuditService auditService;

    private final EventProbe eventProbe;

    private final ObjectMapper objectMapper;

    IdentityVerificationService(
            ThirdPartyFraudGateway thirdPartyGateway,
            PersonIdentityValidator personIdentityValidator,
            ContraindicationMapper contraindicationMapper,
            IdentityScoreCalculator identityScoreCalculator,
            AuditService auditService,
            ConfigurationService configurationService,
            EventProbe eventProbe) {
        this.thirdPartyGateway = thirdPartyGateway;
        this.personIdentityValidator = personIdentityValidator;
        this.contraindicationMapper = contraindicationMapper;
        this.identityScoreCalculator = identityScoreCalculator;
        this.auditService = auditService;
        this.configurationService = configurationService;
        this.eventProbe = eventProbe;

        this.objectMapper = new ObjectMapper();
    }

    public IdentityVerificationResult verifyIdentity(
            PersonIdentity personIdentity,
            SessionItem sessionItem,
            Map<String, String> requestHeaders)
            throws JsonProcessingException, SqsException {
        IdentityVerificationResult identityVerificationResult = new IdentityVerificationResult();

        LOGGER.info("Validating PersonIdentity...");
        ValidationResult<List<String>> validationResult =
                this.personIdentityValidator.validate(personIdentity);
        if (!validationResult.isValid()) {
            identityVerificationResult.setSuccess(false);
            identityVerificationResult.setValidationErrors(validationResult.getError());
            identityVerificationResult.setError("PersonIdentityValidationError");
            eventProbe.counterMetric(PERSON_DETAILS_VALIDATION_FAIL);
            return identityVerificationResult;
        }
        LOGGER.info("PersonIdentity validated");
        eventProbe.counterMetric(PERSON_DETAILS_VALIDATION_PASS);

        // Requests split into two try blocks to differentiate tech failures in fraud from pep
        FraudCheckResult fraudCheckResult;
        try {
            fraudCheckResult = thirdPartyGateway.performFraudCheck(personIdentity, false);
        } catch (InterruptedException ie) {
            LOGGER.error(ERROR_MSG_CONTEXT, ie);
            eventProbe.counterMetric(FRAUD_CHECK_REQUEST_FAILED);

            Thread.currentThread().interrupt();
            identityVerificationResult.setError(ERROR_MSG_CONTEXT + ": " + ie.getMessage());
            identityVerificationResult.setSuccess(false);

            return identityVerificationResult;
        } catch (Exception e) {
            LOGGER.error(ERROR_MSG_CONTEXT, e);
            eventProbe.counterMetric(FRAUD_CHECK_REQUEST_FAILED);

            identityVerificationResult.setError(ERROR_MSG_CONTEXT + ": " + e.getMessage());
            identityVerificationResult.setSuccess(false);

            return identityVerificationResult;
        }

        String loggedFraudCheckObject = objectMapper.writeValueAsString(fraudCheckResult);
        LOGGER.info("Third party fraud response {}", loggedFraudCheckObject);

        // fraudCheckResult null on exceptions
        if (null == fraudCheckResult) {
            LOGGER.error(ERROR_FRAUD_CHECK_RESULT_RETURN_NULL);
            eventProbe.counterMetric(FRAUD_CHECK_REQUEST_FAILED);

            identityVerificationResult.setError(ERROR_MSG_CONTEXT);
            identityVerificationResult.setSuccess(false);

            return identityVerificationResult;
        }

        if (!fraudCheckResult.isExecutedSuccessfully()) {
            LOGGER.warn("Fraud check failed");
            eventProbe.counterMetric(FRAUD_CHECK_REQUEST_FAILED);

            // Networking failure or Error Response in FraudCheck
            identityVerificationResult.setSuccess(false);

            if (Objects.nonNull(fraudCheckResult.getErrorMessage())) {
                identityVerificationResult.setError(fraudCheckResult.getErrorMessage());
            } else {
                identityVerificationResult.setError(ERROR_FRAUD_CHECK_RESULT_NO_ERR_MSG);
                LOGGER.warn(ERROR_FRAUD_CHECK_RESULT_NO_ERR_MSG);
            }

            return identityVerificationResult;
        }

        // FraudCheck has now succeeded and result can be returned without pepCheck succeeding
        identityVerificationResult.setSuccess(true);

        // For creating check details / failed check details
        List<String> checksSucceeded = new ArrayList<>();
        List<String> checksFailed = new ArrayList<>();

        LOGGER.info("Mapping contra indicators from fraud response");
        List<String> fraudContraindications =
                List.of(
                        this.contraindicationMapper.mapThirdPartyFraudCodes(
                                fraudCheckResult.getThirdPartyFraudCodes()));

        LOGGER.info(
                "Third party decision score {} and fraud codes {}",
                fraudCheckResult.getDecisionScore(),
                Arrays.toString(fraudCheckResult.getThirdPartyFraudCodes()));

        int fraudIdentityCheckScore =
                identityScoreCalculator.calculateIdentityScore(fraudCheckResult, false);
        LOGGER.info(
                "Fraud check passed successfully. Indicators {}, Score {}",
                String.join(", ", fraudContraindications),
                fraudIdentityCheckScore);
        eventProbe.counterMetric(FRAUD_CHECK_REQUEST_SUCCEEDED);

        // For deciding if a pepCheck should be done
        int decisionScore =
                fraudCheckResult.getDecisionScore() != null
                        ? Integer.valueOf(fraudCheckResult.getDecisionScore())
                        : 0;

        // Pep Check
        Integer pepIdentityCheckScore = null;
        List<String> pepContraindications = new ArrayList<>();
        String pepTransactionId = null;

        // fraudIdentityCheckScore must be also one at this stage to perform pepCheck
        // (no zero score uCode)
        if ((decisionScore > configurationService.getNoFileFoundThreshold())
                && fraudIdentityCheckScore == 1) {
            if (configurationService.getPepEnabled()) {

                FraudCheckResult pepCheckResult = null;

                try {
                    pepCheckResult = thirdPartyGateway.performFraudCheck(personIdentity, true);
                } catch (InterruptedException ie) {
                    // This handles an IE occurring when doing the sleep in the backoff
                    LOGGER.error(ERROR_MSG_CONTEXT, ie);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    // Pep check can completely fail and result returned based on fraud check alone
                    LOGGER.error(ERROR_MSG_CONTEXT, e);
                }

                String loggedPepCheckObject = objectMapper.writeValueAsString(pepCheckResult);
                LOGGER.info("Third party pep response {}", loggedPepCheckObject);

                // pepCheckResult null on exceptions
                if (null != pepCheckResult && pepCheckResult.isExecutedSuccessfully()) {

                    LOGGER.info("Mapping contra indicators from pep response");
                    pepContraindications =
                            List.of(
                                    this.contraindicationMapper.mapThirdPartyFraudCodes(
                                            pepCheckResult.getThirdPartyFraudCodes()));
                    pepIdentityCheckScore =
                            identityScoreCalculator.calculateIdentityScore(
                                    fraudCheckResult, pepCheckResult.isExecutedSuccessfully());
                    pepTransactionId = pepCheckResult.getTransactionId();

                    // IPR is present if a PEP check has been performed successfully irrelevant of
                    // result
                    checksSucceeded.add(IMPERSONATION_RISK_CHECK.toString());
                    identityVerificationResult.setPepTransactionId(pepTransactionId);

                    LOGGER.info(
                            "Pep check passed successfully. Indicators {}, Score {}",
                            String.join(", ", pepContraindications),
                            pepIdentityCheckScore);
                    eventProbe.counterMetric(PEP_CHECK_REQUEST_SUCCEEDED);

                } else {
                    // IPR is set as failed if the PEP check has been attempted but failed
                    checksFailed.add(IMPERSONATION_RISK_CHECK.toString());
                    identityVerificationResult.setPepTransactionId(pepTransactionId);

                    LOGGER.warn("Pep check failed");
                    eventProbe.counterMetric(PEP_CHECK_REQUEST_FAILED);
                }
            }

            // Fraud Checks that have succeeded if decisionScore > NoFileFoundThreshold
            checksSucceeded.add(MORTALITY_CHECK.toString());
            checksSucceeded.add(IDENTITY_THEFT_CHECK.toString());
            checksSucceeded.add(SYNTHETIC_IDENTITY_CHECK.toString());
        } else {
            // Fraud Checks that have failed if decisionScore <= NoFileFoundThreshold
            checksFailed.add(MORTALITY_CHECK.toString());
            checksFailed.add(IDENTITY_THEFT_CHECK.toString());
            checksFailed.add(SYNTHETIC_IDENTITY_CHECK.toString());

            LOGGER.info(
                    "User was file not found with decision score {} so PEP checks have been skipped",
                    decisionScore);
        }

        LOGGER.info("Calculating the identity score...");
        int identityCheckScore =
                pepIdentityCheckScore != null ? pepIdentityCheckScore : fraudIdentityCheckScore;
        LOGGER.info("IdentityCheckScore {}", identityCheckScore);
        eventProbe.counterMetric(IDENTITY_CHECK_SCORE_PREFIX + identityCheckScore);
        identityVerificationResult.setIdentityCheckScore(identityCheckScore);

        // Record Combined CI's
        List<String> combinedContraIndicators = new ArrayList<>();
        combinedContraIndicators.addAll(fraudContraindications);
        combinedContraIndicators.addAll(pepContraindications);
        identityVerificationResult.setContraIndicators(
                combinedContraIndicators.toArray(new String[] {}));

        // Per-request contra-indicator metrics
        recordCIMetrics(FRAUD_CHECK_CI_PREFIX, fraudContraindications);
        recordCIMetrics(PEP_CHECK_CI_PREFIX, pepContraindications);

        // Record transaction ids
        String fraudTransactionId = fraudCheckResult.getTransactionId();
        LOGGER.info(
                "Third party transaction ids fraud {} pep {}",
                fraudTransactionId,
                pepTransactionId);
        identityVerificationResult.setTransactionId(fraudTransactionId);
        identityVerificationResult.setPepTransactionId(pepTransactionId);

        identityVerificationResult.setDecisionScore(String.valueOf(decisionScore));

        // Record checks status
        identityVerificationResult.setChecksSucceeded(checksSucceeded);
        identityVerificationResult.setChecksFailed(checksFailed);

        auditService.sendAuditEvent(
                AuditEventType.THIRD_PARTY_REQUEST_ENDED,
                new AuditEventContext(requestHeaders, sessionItem),
                new TPREFraudAuditExtension(List.of(fraudCheckResult.getThirdPartyFraudCodes())));

        return identityVerificationResult;
    }

    private void recordCIMetrics(String ciRequestPrefix, List<String> contraIndications) {
        for (String ci : contraIndications) {
            eventProbe.counterMetric(ciRequestPrefix + ci);
        }
    }
}
