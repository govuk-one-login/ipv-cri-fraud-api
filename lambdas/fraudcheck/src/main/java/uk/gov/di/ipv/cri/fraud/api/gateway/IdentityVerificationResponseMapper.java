package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.DecisionElement;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.ResponseHeader;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.ResponseType;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.Rule;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationInfoResponseValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IdentityVerificationResponseMapper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ERROR_MESSAGE_FORMAT = "Error code: %s, error description: %s";
    private static final String DF_VALUE_REPLACEMENT = "Not specified";
    private static final String IV_INFO_RESPONSE_VALIDATION_FAILED_MSG =
            "Identity Verification Info Response failed validation.";

    FraudCheckResult mapIdentityVerificationResponse(IdentityVerificationResponse response) {
        ResponseType responseType = response.getResponseHeader().getResponseType();

        switch (responseType) {
            case ERROR:
            case WARN:
                return mapErrorResponse(response.getResponseHeader());
            case WARNING:
                return mapErrorResponse(response.getResponseHeader());
            case INFO:
                return mapResponse(response, new IdentityVerificationInfoResponseValidator());
            default:
                throw new IllegalArgumentException(
                        "Unexpected response type encountered: " + responseType);
        }
    }

    private FraudCheckResult mapErrorResponse(ResponseHeader responseHeader) {
        FraudCheckResult identityContraindication = new FraudCheckResult();
        identityContraindication.setExecutedSuccessfully(false);
        identityContraindication.setErrorMessage(
                String.format(
                        ERROR_MESSAGE_FORMAT,
                        replaceIfBlank(responseHeader.getResponseCode()),
                        replaceIfBlank(responseHeader.getResponseMessage())));
        return identityContraindication;
    }

    private FraudCheckResult mapResponse(
            IdentityVerificationResponse response,
            IdentityVerificationInfoResponseValidator infoResponseValidator) {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();

        ValidationResult<List<String>> validationResult = infoResponseValidator.validate(response);

        if (validationResult.isValid()) {
            fraudCheckResult.setExecutedSuccessfully(true);

            List<DecisionElement> decisionElements =
                    response.getClientResponsePayload().getDecisionElements();

            List<String> fraudCodes = new ArrayList<>();

            for (DecisionElement decisionElement : decisionElements) {
                decisionElement.getRules().stream()
                        .map(Rule::getRuleId)
                        .filter(StringUtils::isNotBlank)
                        .sequential()
                        .collect(Collectors.toCollection(() -> fraudCodes));
            }

            fraudCheckResult.setThirdPartyFraudCodes(
                    fraudCodes.toArray(fraudCodes.toArray(String[]::new)));
        } else {
            fraudCheckResult.setExecutedSuccessfully(false);
            fraudCheckResult.setErrorMessage(IV_INFO_RESPONSE_VALIDATION_FAILED_MSG);

            LOGGER.error(
                    () -> (IV_INFO_RESPONSE_VALIDATION_FAILED_MSG + validationResult.getError()));
        }
        fraudCheckResult.setTransactionId(response.getResponseHeader().getExpRequestId());
        return fraudCheckResult;
    }

    private String replaceIfBlank(String input) {
        return StringUtils.isBlank(input) ? DF_VALUE_REPLACEMENT : input;
    }
}
