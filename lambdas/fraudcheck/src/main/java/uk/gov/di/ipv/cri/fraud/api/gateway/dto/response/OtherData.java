package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OtherData {

    @JsonProperty("authResults")
    private AuthResults authResults;

    public AuthResults getAuthResults() {
        return authResults;
    }

    public void setAuthResults(AuthResults authResults) {
        this.authResults = authResults;
    }
}
