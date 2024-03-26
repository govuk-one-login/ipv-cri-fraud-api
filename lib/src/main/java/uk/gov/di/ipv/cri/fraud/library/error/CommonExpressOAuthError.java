package uk.gov.di.ipv.cri.fraud.library.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nimbusds.oauth2.sdk.ErrorObject;
import net.minidev.json.JSONObject;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public class CommonExpressOAuthError {
    @JsonProperty("oauth_error")
    private final JSONObject error;

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

    public JSONObject getError() {
        return error;
    }
}
