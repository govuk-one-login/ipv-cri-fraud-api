package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthPlusResults {

    @JsonProperty("authConsumer")
    private AuthConsumer authConsumer;

    public AuthConsumer getAuthConsumer() {
        return authConsumer;
    }

    public void setAuthConsumer(AuthConsumer authConsumer) {
        this.authConsumer = authConsumer;
    }
}
