package uk.gov.di.ipv.cri.fraud.library.exception;

import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;

public class OAuthErrorResponseException extends Exception {
    private final int statusCode;
    private final ErrorResponse errorResponse;

    public OAuthErrorResponseException(int statusCode, ErrorResponse errorResponse) {
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }

    public String getErrorReason() {
        return this.errorResponse.getMessage();
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
