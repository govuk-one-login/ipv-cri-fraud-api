package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtherData {

    @JsonProperty("response")
    private String response;

    @JsonProperty("authResults")
    private AuthResults authResults;

    public AuthResults getAuthResults() {
        return authResults;
    }

    public void setAuthResults(AuthResults authResults) {
        this.authResults = authResults;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
