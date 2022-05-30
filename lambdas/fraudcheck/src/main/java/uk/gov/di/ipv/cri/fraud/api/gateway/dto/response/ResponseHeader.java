package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseHeader {

    @JsonProperty("requestType")
    private String requestType;

    @JsonProperty("clientReferenceId")
    private String clientReferenceId;

    @JsonProperty("expRequestId")
    private String expRequestId;

    @JsonProperty("messageTime")
    private String messageTime;

    @JsonProperty("overallResponse")
    private OverallResponse overallResponse;

    @JsonProperty("responseCode")
    private String responseCode;

    @JsonProperty("responseType")
    private ResponseType responseType;

    @JsonProperty("responseMessage")
    private String responseMessage;

    @JsonProperty("tenantID")
    private String tenantID;

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

    public OverallResponse getOverallResponse() {
        return overallResponse;
    }

    public void setOverallResponse(OverallResponse overallResponse) {
        this.overallResponse = overallResponse;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getTenantID() {
        return tenantID;
    }

    public void setTenantID(String tenantID) {
        this.tenantID = tenantID;
    }
}
