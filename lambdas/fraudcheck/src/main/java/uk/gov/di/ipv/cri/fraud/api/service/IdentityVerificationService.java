package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;

import java.util.Arrays;
import java.util.List;
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
    private final IdentityScoreCalaculator identityScoreCalaculator;

    IdentityVerificationService(
            ThirdPartyFraudGateway thirdPartyGateway,
            PersonIdentityValidator personIdentityValidator,
            ContraindicationMapper contraindicationMapper,
            IdentityScoreCalaculator identityScoreCalaculator) {
        this.thirdPartyGateway = thirdPartyGateway;
        this.personIdentityValidator = personIdentityValidator;
        this.contraindicationMapper = contraindicationMapper;
        this.identityScoreCalaculator = identityScoreCalaculator;
    }

    public IdentityVerificationResult verifyIdentity(PersonIdentity personIdentity) {
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

            FraudCheckResult fraudCheckResult = thirdPartyGateway.performFraudCheck(personIdentity);
            LOGGER.info("Third party response mapped");
            LOGGER.info(
                    "Third party response {}",
                    new ObjectMapper().writeValueAsString(fraudCheckResult));

            if (Objects.nonNull(fraudCheckResult)) {
                result.setSuccess(fraudCheckResult.isExecutedSuccessfully());
                if (result.isSuccess()) {
                    LOGGER.info("Mapping contra indicators from fraud response");

                    String[] contraindications =
                            this.contraindicationMapper.mapThirdPartyFraudCodes(
                                    fraudCheckResult.getThirdPartyFraudCodes());
                    int identityCheckScore =
                            identityScoreCalaculator.calculateIdentityScore(
                                    fraudCheckResult.isExecutedSuccessfully(), contraindications);
                    result.setContraIndicators(contraindications);
                    result.setIdentityCheckScore(identityCheckScore);
                    result.setTransactionId(fraudCheckResult.getTransactionId());

                    LOGGER.info(
                            "Fraud check passed successfully. Indicators {}, Score {}",
                            Arrays.toString(contraindications),
                            identityCheckScore);
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
