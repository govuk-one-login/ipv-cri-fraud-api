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
    public static final String IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT =
            "Error code: %s, error description: %s";
    public static final String IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK =
            "Not specified";
    public static final String IV_INFO_RESPONSE_VALIDATION_FAILED_MSG =
            "Identity Verification Info Response failed validation.";

    FraudCheckResult mapIdentityVerificationResponse(IdentityVerificationResponse response) {
        ResponseType responseType = response.getResponseHeader().getResponseType();

        switch (responseType) {
            case INFO:
                return mapResponse(response, new IdentityVerificationInfoResponseValidator());
            case ERROR:
            case WARN:
            case WARNING:
                return mapErrorResponse(response.getResponseHeader());
            default:
                throw new IllegalArgumentException(
                        "Unmapped response type encountered: " + responseType);
        }
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

    private FraudCheckResult mapErrorResponse(ResponseHeader responseHeader) {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();
        fraudCheckResult.setExecutedSuccessfully(false);
        fraudCheckResult.setErrorMessage(
                String.format(
                        IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        replaceWithDefaultErrorValueIfBlank(responseHeader.getResponseCode()),
                        replaceWithDefaultErrorValueIfBlank(responseHeader.getResponseMessage())));
        return fraudCheckResult;
    }

    private String replaceWithDefaultErrorValueIfBlank(String input) {
        return StringUtils.isBlank(input)
                ? IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK
                : input;
    }
}
