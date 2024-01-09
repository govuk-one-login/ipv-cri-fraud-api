package uk.gov.di.ipv.cri.fraud.library.exception;

public class TokenExpiryWindowException extends RuntimeException {
    public TokenExpiryWindowException(String message) {
        super(message);
    }
}
