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

        // For creating check details / failed check details
        List<String> checksSucceeded = new ArrayList<>();
        List<String> checksFailed = new ArrayList<>();
        List<String> combinedContraIndicators = new ArrayList<>();

        identityVerificationResult.setChecksSucceeded(checksSucceeded);
        identityVerificationResult.setChecksFailed(checksFailed);
        identityVerificationResult.setContraIndicators(combinedContraIndicators);

        boolean pepValidToPerform = fraudCheckStep(identityVerificationResult, personIdentity);

        if (pepValidToPerform && configurationService.getPepEnabled()) {
            pepCheckStep(identityVerificationResult, personIdentity);
        }

        if (identityVerificationResult.isSuccess()) {
            int identityCheckScore = identityVerificationResult.getIdentityCheckScore();
            LOGGER.info("Final IdentityCheckScore {}", identityCheckScore);
            eventProbe.counterMetric(IDENTITY_CHECK_SCORE_PREFIX + identityCheckScore);

            LOGGER.info(
                    "Third party transaction ids fraud {} pep {}",
                    identityVerificationResult.getTransactionId(),
                    identityVerificationResult.getPepTransactionId());

            String stringCIs = String.join(", ", identityVerificationResult.getContraIndicators());
            LOGGER.info("Final Combined Indicators {}", stringCIs);

            auditService.sendAuditEvent(
                    AuditEventType.THIRD_PARTY_REQUEST_ENDED,
                    new AuditEventContext(requestHeaders, sessionItem),
                    new TPREFraudAuditExtension(identityVerificationResult.getContraIndicators()));
        }

        return identityVerificationResult;
    }

    public boolean fraudCheckStep(
            IdentityVerificationResult identityVerificationResult, PersonIdentity personIdentity)
            throws JsonProcessingException {
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

            return false;
        } catch (Exception e) {
            LOGGER.error(ERROR_MSG_CONTEXT, e);
            eventProbe.counterMetric(FRAUD_CHECK_REQUEST_FAILED);

            identityVerificationResult.setError(ERROR_MSG_CONTEXT + ": " + e.getMessage());
            identityVerificationResult.setSuccess(false);

            return false;
        }

        String loggedFraudCheckObject = objectMapper.writeValueAsString(fraudCheckResult);
        LOGGER.info("Third party fraud response {}", loggedFraudCheckObject);

        // fraudCheckResult null on exceptions
        if (null == fraudCheckResult) {
            LOGGER.error(ERROR_FRAUD_CHECK_RESULT_RETURN_NULL);
            eventProbe.counterMetric(FRAUD_CHECK_REQUEST_FAILED);

            identityVerificationResult.setError(ERROR_MSG_CONTEXT);
            identityVerificationResult.setSuccess(false);

            return false;
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

            return false;
        }

        // FraudCheck has now succeeded and result can be returned without pepCheck succeeding
        identityVerificationResult.setSuccess(true);
        identityVerificationResult.setTransactionId(fraudCheckResult.getTransactionId());

        List<String> checksSucceeded = identityVerificationResult.getChecksSucceeded();
        List<String> checksFailed = identityVerificationResult.getChecksFailed();
        List<String> combinedContraIndicators = identityVerificationResult.getContraIndicators();

        LOGGER.info("Mapping contra indicators from fraud response");
        List<String> fraudContraindications =
                List.of(
                        this.contraindicationMapper.mapThirdPartyFraudCodes(
                                fraudCheckResult.getThirdPartyFraudCodes()));

        // Record FraudCheck CI's
        recordCIMetrics(FRAUD_CHECK_CI_PREFIX, fraudContraindications);
        combinedContraIndicators.addAll(fraudContraindications);

        String thirdPartyFraudCodes = Arrays.toString(fraudCheckResult.getThirdPartyFraudCodes());
        LOGGER.info(
                "Third party decision score {} and fraud codes {}",
                fraudCheckResult.getDecisionScore(),
                thirdPartyFraudCodes);

        int fraudIdentityCheckScore =
                identityScoreCalculator.calculateIdentityScoreAfterFraudCheck(
                        fraudCheckResult, true);

        // For deciding if a pepCheck should be done
        int decisionScore = Integer.parseInt(fraudCheckResult.getDecisionScore());
        identityVerificationResult.setDecisionScore(fraudCheckResult.getDecisionScore());

        LOGGER.info("IdentityCheckScore after Fraud {}", fraudIdentityCheckScore);
        identityVerificationResult.setIdentityCheckScore(fraudIdentityCheckScore);

        String stringFraudContraindications = String.join(", ", fraudContraindications);
        LOGGER.info(
                "Fraud check performed successfully. Indicators {}, Score {}",
                stringFraudContraindications,
                fraudIdentityCheckScore);
        eventProbe.counterMetric(FRAUD_CHECK_REQUEST_SUCCEEDED);

        if (decisionScore <= configurationService.getNoFileFoundThreshold()) {

            // Fraud Checks that have failed if decisionScore <= NoFileFoundThreshold
            checksFailed.add(MORTALITY_CHECK.toString());
            checksFailed.add(IDENTITY_THEFT_CHECK.toString());
            checksFailed.add(SYNTHETIC_IDENTITY_CHECK.toString());

            LOGGER.info(
                    "User was file not found with decision score {} so PEP check will be skipped",
                    decisionScore);

            // No Pep check
            return false;
        }

        // fraudIdentityCheckScore must be one to perform pepCheck (no zero score uCode)
        if (fraudIdentityCheckScore != 1) {

            LOGGER.info(
                    "fraudIdentityCheckScore {} so PEP check will be skipped",
                    fraudIdentityCheckScore);

            checksFailed.add(MORTALITY_CHECK.toString());
            checksFailed.add(IDENTITY_THEFT_CHECK.toString());
            checksFailed.add(SYNTHETIC_IDENTITY_CHECK.toString());

            return false;
        }

        // Fraud Checks that have succeeded if decisionScore > NoFileFoundThreshold and score
        // currently 1
        checksSucceeded.add(MORTALITY_CHECK.toString());
        checksSucceeded.add(IDENTITY_THEFT_CHECK.toString());
        checksSucceeded.add(SYNTHETIC_IDENTITY_CHECK.toString());

        // Pep check is now available
        return true;
    }

    public boolean pepCheckStep(
            IdentityVerificationResult identityVerificationResult, PersonIdentity personIdentity)
            throws JsonProcessingException {

        List<String> checksSucceeded = identityVerificationResult.getChecksSucceeded();
        List<String> checksFailed = identityVerificationResult.getChecksFailed();
        List<String> combinedContraIndicators = identityVerificationResult.getContraIndicators();

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
            List<String> pepContraindications =
                    List.of(
                            this.contraindicationMapper.mapThirdPartyFraudCodes(
                                    pepCheckResult.getThirdPartyFraudCodes()));
            int pepIdentityCheckScore =
                    identityScoreCalculator.calculateIdentityScoreAfterPEPCheck(
                            identityVerificationResult.getIdentityCheckScore(),
                            pepCheckResult.isExecutedSuccessfully());

            LOGGER.info("IdentityCheckScore after PEP {}", pepIdentityCheckScore);
            identityVerificationResult.setIdentityCheckScore(pepIdentityCheckScore);

            // IPR is present if a PEP check has been performed successfully irrelevant of
            // result
            checksSucceeded.add(IMPERSONATION_RISK_CHECK.toString());
            identityVerificationResult.setPepTransactionId(pepCheckResult.getTransactionId());

            String stringPepContraindications = String.join(", ", pepContraindications);
            LOGGER.info(
                    "Pep check passed successfully. Indicators {}, Score {}",
                    stringPepContraindications,
                    pepIdentityCheckScore);

            // Record PEP CI's
            recordCIMetrics(PEP_CHECK_CI_PREFIX, pepContraindications);
            combinedContraIndicators.addAll(pepContraindications);

            eventProbe.counterMetric(PEP_CHECK_REQUEST_SUCCEEDED);

            return true;
        }

        // IPR is set as failed if the PEP check has been attempted but failed
        checksFailed.add(IMPERSONATION_RISK_CHECK.toString());

        LOGGER.warn("Pep check failed");
        eventProbe.counterMetric(PEP_CHECK_REQUEST_FAILED);

        return false;
    }

    private void recordCIMetrics(String ciRequestPrefix, List<String> contraIndications) {
        for (String ci : contraIndications) {
            eventProbe.counterMetric(ciRequestPrefix + ci);
        }
    }
}
