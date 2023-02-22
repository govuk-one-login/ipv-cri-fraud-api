package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResults {

    @JsonProperty("authPlusResults")
    private AuthPlusResults authPlusResults;

    public AuthPlusResults getAuthPlusResults() {
        return authPlusResults;
    }

    public void setAuthPlusResults(AuthPlusResults authPlusResults) {
        this.authPlusResults = authPlusResults;
    }
}
