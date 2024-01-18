package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonCreator
    public TokenResponse(
            @JsonProperty(value = "access_token", required = true) String accessToken) {
        this.accessToken = accessToken;
    }
}
