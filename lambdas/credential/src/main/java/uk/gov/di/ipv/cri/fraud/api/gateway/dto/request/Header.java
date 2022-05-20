package uk.gov.di.ipv.cri.fraud.api.gateway.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "tenantId",
    "requestType",
    "clientReferenceId",
    "expRequestId",
    "messageTime",
    "options"
})
public class Header {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("requestType")
    private String requestType;

    @JsonProperty("clientReferenceId")
    private String clientReferenceId;

    @JsonProperty("expRequestId")
    private String expRequestId;

    @JsonProperty("messageTime")
    private String messageTime;

    @JsonProperty("options")
    private Options options;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getClientReferenceId() {
        return clientReferenceId;
    }

    public void setClientReferenceId(String clientReferenceId) {
        this.clientReferenceId = clientReferenceId;
    }

    public String getExpRequestId() {
        return expRequestId;
    }

    public void setExpRequestId(String expRequestId) {
        this.expRequestId = expRequestId;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }
}
