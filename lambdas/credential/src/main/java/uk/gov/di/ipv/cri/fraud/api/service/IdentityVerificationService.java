package uk.gov.di.ipv.cri.fraud.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;
import uk.gov.di.ipv.cri.fraud.library.validation.InputValidationExecutor;
import uk.gov.di.ipv.cri.fraud.library.validation.ValidationResult;

import java.util.List;
import java.util.Objects;

public class IdentityVerificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityVerificationService.class);
    private static final String ERROR_MSG_CONTEXT =
            "Error occurred when attempting to invoke the third party api";
    private final ThirdPartyFraudGateway thirdPartyGateway;
    private final InputValidationExecutor inputValidationExecutor;
    private final ContraindicationMapper contraindicationMapper;

    IdentityVerificationService(
            ThirdPartyFraudGateway thirdPartyGateway,
            InputValidationExecutor inputValidationExecutor,
            ContraindicationMapper contraindicationMapper) {
        this.thirdPartyGateway = thirdPartyGateway;
        this.inputValidationExecutor = inputValidationExecutor;
        this.contraindicationMapper = contraindicationMapper;
    }

    public IdentityVerificationResult verifyIdentity(PersonIdentity personIdentity) {
        IdentityVerificationResult result = new IdentityVerificationResult();
        try {
            ValidationResult<List<String>> validationResult =
                    this.inputValidationExecutor.performInputValidation(personIdentity);

            if (!validationResult.isValid()) {
                result.setSuccess(false);
                result.setValidationErrors(validationResult.getError());
                return result;
            }

            FraudCheckResult fraudCheckResult = thirdPartyGateway.performFraudCheck(personIdentity);

            if (Objects.nonNull(fraudCheckResult)) {
                result.setSuccess(fraudCheckResult.isExecutedSuccessfully());
                if (result.isSuccess()) {
                    String[] contraindications =
                            this.contraindicationMapper.mapThirdPartyFraudCodes(
                                    fraudCheckResult.getThirdPartyFraudCodes());
                    result.setContraIndicators(contraindications);
                }
                return result;
            }

            LOGGER.error("Unexpected null returned when invoking third party API");
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
