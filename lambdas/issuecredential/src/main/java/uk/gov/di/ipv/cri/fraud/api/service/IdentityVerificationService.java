package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;

import java.util.List;
import java.util.Objects;

public class IdentityVerificationService {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ERROR_MSG_CONTEXT =
            "Error occurred when attempting to invoke the third party api";
    private final ThirdPartyFraudGateway thirdPartyGateway;
    private final PersonIdentityValidator personIdentityValidator;
    private final ContraindicationMapper contraindicationMapper;

    IdentityVerificationService(
            ThirdPartyFraudGateway thirdPartyGateway,
            PersonIdentityValidator personIdentityValidator,
            ContraindicationMapper contraindicationMapper) {
        this.thirdPartyGateway = thirdPartyGateway;
        this.personIdentityValidator = personIdentityValidator;
        this.contraindicationMapper = contraindicationMapper;
    }

    public IdentityVerificationResult verifyIdentity(PersonIdentity personIdentity) {
        IdentityVerificationResult result = new IdentityVerificationResult();
        try {
            ValidationResult<List<String>> validationResult =
                    this.personIdentityValidator.validate(personIdentity);

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
