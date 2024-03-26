package uk.gov.di.ipv.cri.fraud.library.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.oauth2.sdk.ErrorObject;
import net.minidev.json.JSONObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonExpressOAuthError {

    /* OAuth Redirect */
    @JsonProperty("oauth_error")
    private final JSONObject error;

    /* Internal State */
    @JsonProperty("cri_internal_error_code")
    private String criInternalErrorCode;

    @JsonProperty("cri_internal_error_message")
    private String criInternalErrorMessage;

    /**
     * @param errorObject For the error to be standards compliant it should be one of OAuth2Error
     * @param customOAuth2ErrorDescription A message to be forwarded to the client to indicate the
     *     error reason
     */
    public CommonExpressOAuthError(ErrorObject errorObject, String customOAuth2ErrorDescription) {
        this.error = errorObject.setDescription(customOAuth2ErrorDescription).toJSONObject();
    }

    /**
     * @param errorObject For the error to be standards compliant it should be one of OAuth2Error,
     *     the error description then be determined by the OAuth2Error object chosen.
     */
    public CommonExpressOAuthError(ErrorObject errorObject) {
        error = errorObject.toJSONObject();
    }

    /**
     * Only to be set in Dev - allows backend API tests to assert the error state of the cri
     *
     * @param errorResponse
     */
    public void setCriInternalErrorState(ErrorResponse errorResponse) {
        this.criInternalErrorCode = String.valueOf(errorResponse.getCode());
        this.criInternalErrorMessage = errorResponse.getMessage();
    }

    public JSONObject getError() {
        return error;
    }
}
