package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true, value = "originalRequestData")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdentityVerificationResponse {
    @JsonProperty("responseHeader")
    private ResponseHeader responseHeader;

    @JsonProperty("clientResponsePayload")
    private ClientResponsePayload clientResponsePayload;

    public ResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(ResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
    }

    public ClientResponsePayload getClientResponsePayload() {
        return clientResponsePayload;
    }

    public void setClientResponsePayload(ClientResponsePayload clientResponsePayload) {
        this.clientResponsePayload = clientResponsePayload;
    }
}
