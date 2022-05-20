package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.DecisionElement;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.ResponseHeader;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.ResponseType;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.Rule;

import java.util.Objects;

public class IdentityVerificationResponseMapper {

    private static final String ERROR_MESSAGE_FORMAT = "Error code: %s, error description: %s";
    private static final String DF_VALUE_REPLACEMENT = "Not specified";

    FraudCheckResult mapIdentityVerificationResponse(IdentityVerificationResponse response) {
        ResponseType responseType = response.getResponseHeader().getResponseType();

        switch (responseType) {
            case ERROR:
            case WARN:
                return mapErrorResponse(response.getResponseHeader());
            case INFO:
                return mapResponse(response);
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

    private FraudCheckResult mapResponse(IdentityVerificationResponse response) {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();
        fraudCheckResult.setExecutedSuccessfully(true);
        if (Objects.nonNull(response.getClientResponsePayload())
                && !response.getClientResponsePayload().getDecisionElements().isEmpty()) {
            DecisionElement decisionElement =
                    response.getClientResponsePayload().getDecisionElements().get(0);
            if (Objects.nonNull(decisionElement.getRules())
                    && !decisionElement.getRules().isEmpty()) {
                String[] fraudCodes =
                        decisionElement.getRules().stream()
                                .map(Rule::getRuleId)
                                .filter(StringUtils::isNotBlank)
                                .toArray(String[]::new);
                fraudCheckResult.setThirdPartyFraudCodes(fraudCodes);
            }
        }
        return fraudCheckResult;
    }

    private String replaceIfBlank(String input) {
        return StringUtils.isBlank(input) ? DF_VALUE_REPLACEMENT : input;
    }
}
