package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
