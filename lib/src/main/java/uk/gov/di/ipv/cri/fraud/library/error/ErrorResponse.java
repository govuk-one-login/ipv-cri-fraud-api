package uk.gov.di.ipv.cri.fraud.library.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorResponse {
    FAILED_TO_RETRIEVE_PERSON_IDENTITY(1000, "Failed to retrieve retrieve personIdentity"),
    FORM_DATA_FAILED_VALIDATION(1001, "Form Data failed validation"),
    TOO_MANY_RETRY_ATTEMPTS(1002, "Too many retry attempts made"),

    IDENTITY_VERIFICATION_UNSUCCESSFUL(1003, "identity verification unsuccessful"),

    FAILED_TO_RETRIEVE_HTTP_RESPONSE_BODY(1099, "Failed to retrieve http response body"),

    /**************************************Fraud Check Specific Errors************************************/

    FAILED_TO_CREATE_API_REQUEST_FOR_FRAUD_CHECK(
            2001, "Failed to create API Request for Fraud Check"),
    ERROR_SENDING_FRAUD_CHECK_REQUEST(2002, "error sending Fraud check request"),
    FAILED_TO_MAP_FRAUD_CHECK_RESPONSE_BODY(2003, "failed to map Fraud check response_body"),
    ERROR_FRAUD_CHECK_RETURNED_UNEXPECTED_HTTP_STATUS_CODE(
            2004, "error Fraud check returned unexpected http status code"),
    /**************************************PEP Check Specific Errors************************************/

    FAILED_TO_CREATE_API_REQUEST_FOR_PEP_CHECK(3001, "Failed to create API Request for PEP Check"),
    ERROR_SENDING_PEP_CHECK_REQUEST(3002, "error sending PEP check request"),
    FAILED_TO_MAP_PEP_CHECK_RESPONSE_BODY(3003, "failed to map PEP check response_body"),
    ERROR_PEP_CHECK_RETURNED_UNEXPECTED_HTTP_STATUS_CODE(
            3004, "error PEP check returned unexpected http status code"),

    /**********************************Authenticate Call Specific Errors************************************/

    ERROR_INVOKING_THIRD_PARTY_API_TOKEN_ENDPOINT(
            4001, "Error occurred when attempting to invoke the third party api token endpoint"),
    FAILED_TO_PREPARE_TOKEN_REQUEST_PAYLOAD(4002, "failed to prepare token request payload"),
    FAILED_TO_MAP_TOKEN_ENDPOINT_RESPONSE_BODY(4003, "Failed to map token endpoint response body"),
    ERROR_TOKEN_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE(
            4004, "token endpoint returned unexpected http status code"),
    TOKEN_ENDPOINT_RETURNED_JWT_WITH_UNEXPECTED_VALUES_IN_RESPONSE(
            4005, "token endpoint returned unexpected values in JWT"),

    TEST_DATA_STRATEGY_COULD_NOT_MAP_CLIENTID_TO_THIRD_PARTY_ROUTE(
            4006, "could not map EndpointUri from TestDataStrategy logic"),

    FINAL_ERROR(-1, "Final Error");

    private final int code;
    private final String message;

    ErrorResponse(
            @JsonProperty(required = true, value = "code") int code,
            @JsonProperty(required = true, value = "message") String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
