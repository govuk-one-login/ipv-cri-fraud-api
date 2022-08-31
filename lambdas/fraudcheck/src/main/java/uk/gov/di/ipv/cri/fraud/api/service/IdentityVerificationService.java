package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.audit.TPREFraudAuditExtension;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    IdentityVerificationService(
            ThirdPartyFraudGateway thirdPartyGateway,
            PersonIdentityValidator personIdentityValidator,
            ContraindicationMapper contraindicationMapper,
            IdentityScoreCalculator identityScoreCalculator,
            AuditService auditService,
            ConfigurationService configurationService) {
        this.thirdPartyGateway = thirdPartyGateway;
        this.personIdentityValidator = personIdentityValidator;
        this.contraindicationMapper = contraindicationMapper;
        this.identityScoreCalculator = identityScoreCalculator;
        this.auditService = auditService;
        this.configurationService = configurationService;
    }

    public IdentityVerificationResult verifyIdentity(
            PersonIdentity personIdentity,
            SessionItem sessionItem,
            Map<String, String> requestHeaders) {
        IdentityVerificationResult result = new IdentityVerificationResult();
        try {
            LOGGER.info("Validating identity...");
            ValidationResult<List<String>> validationResult =
                    this.personIdentityValidator.validate(personIdentity);
            if (!validationResult.isValid()) {
                result.setSuccess(false);
                result.setValidationErrors(validationResult.getError());
                result.setError("IdentityValidationError");
                return result;
            }
            LOGGER.info("Identity info validated");
            FraudCheckResult fraudCheckResult =
                    thirdPartyGateway.performFraudCheck(personIdentity, false);

            LOGGER.info("Third party response mapped");
            LOGGER.info(
                    "Third party response {}",
                    new ObjectMapper().writeValueAsString(fraudCheckResult));
            if (Objects.nonNull(fraudCheckResult)) {
                result.setSuccess(
                        fraudCheckResult
                                .isExecutedSuccessfully()); // for testing error scenario comment
                // this out + send no postCode in request
                if (result.isSuccess()) {
                    Integer pepIdentityCheckScore = null;
                    List<String> pepContraindications = new ArrayList<>();
                    String pepTransactionId = null;

                    LOGGER.info("Mapping contra indicators from fraud response");
                    List<String> fraudContraindications =
                            List.of(
                                    this.contraindicationMapper.mapThirdPartyFraudCodes(
                                            fraudCheckResult.getThirdPartyFraudCodes()));
                    int fraudIdentityCheckScore =
                            identityScoreCalculator.calculateIdentityScore(
                                    fraudCheckResult.isExecutedSuccessfully(), false);
                    LOGGER.info(
                            "Fraud check passed successfully. Indicators {}, Score {}",
                            String.join(", ", fraudContraindications),
                            fraudIdentityCheckScore);

                    if (configurationService.getPepEnabled()) {
                        FraudCheckResult pepCheckResult =
                                thirdPartyGateway.performFraudCheck(personIdentity, true);
                        pepContraindications =
                                List.of(
                                        this.contraindicationMapper.mapThirdPartyFraudCodes(
                                                pepCheckResult.getThirdPartyFraudCodes()));
                        pepIdentityCheckScore =
                                identityScoreCalculator.calculateIdentityScore(
                                        fraudCheckResult.isExecutedSuccessfully(),
                                        pepCheckResult.isExecutedSuccessfully());
                        pepTransactionId = pepCheckResult.getTransactionId();
                        LOGGER.info(
                                "Third party pep response {}",
                                new ObjectMapper().writeValueAsString(pepCheckResult));
                        LOGGER.info(
                                "Pep check passed successfully. Indicators {}, Score {}",
                                String.join(", ", pepContraindications),
                                pepIdentityCheckScore);
                    }

                    LOGGER.info("Calculating the identity score...");
                    List<String> combinedContraIndicators = new ArrayList<>();
                    combinedContraIndicators.addAll(pepContraindications);
                    combinedContraIndicators.addAll(fraudContraindications);
                    String fraudTransactionId = fraudCheckResult.getTransactionId();
                    int identityCheckScore =
                            pepIdentityCheckScore != null
                                    ? pepIdentityCheckScore
                                    : fraudIdentityCheckScore;
                    String transactionId = fraudTransactionId;
                    LOGGER.info(
                            "Third party transaction ids fraud {} pep {}",
                            fraudTransactionId,
                            pepTransactionId);

                    result.setContraIndicators(combinedContraIndicators.toArray(new String[] {}));
                    result.setIdentityCheckScore(identityCheckScore);
                    result.setTransactionId(transactionId);
                    result.setSuccess(fraudCheckResult.isExecutedSuccessfully());
                    auditService.sendAuditEvent(
                            AuditEventType.THIRD_PARTY_REQUEST_ENDED,
                            new AuditEventContext(requestHeaders, sessionItem),
                            new TPREFraudAuditExtension(
                                    List.of(fraudCheckResult.getThirdPartyFraudCodes())));
                } else {
                    LOGGER.warn("Fraud check failed");
                    if (Objects.nonNull(fraudCheckResult.getErrorMessage())) {
                        result.setError(fraudCheckResult.getErrorMessage());
                    } else {
                        result.setError(ERROR_FRAUD_CHECK_RESULT_NO_ERR_MSG);
                        LOGGER.warn(ERROR_FRAUD_CHECK_RESULT_NO_ERR_MSG);
                    }
                }
                return result;
            }
            LOGGER.error(ERROR_FRAUD_CHECK_RESULT_RETURN_NULL);
            result.setError(ERROR_MSG_CONTEXT);
            result.setSuccess(false);
        } catch (InterruptedException ie) {
            LOGGER.error(ERROR_MSG_CONTEXT, ie);
            Thread.currentThread().interrupt();
            result.setError(ERROR_MSG_CONTEXT + ": " + ie.getMessage());
            result.setSuccess(false);
        } catch (Exception e) {
            LOGGER.error(ERROR_MSG_CONTEXT, e);
            result.setError(ERROR_MSG_CONTEXT + ": " + e.getMessage());
            result.setSuccess(false);
        }
        return result;
    }
}
