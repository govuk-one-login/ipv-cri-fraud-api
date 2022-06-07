package uk.gov.di.ipv.cri.fraud.api.domain;

import java.util.List;

public class IdentityVerificationResult {
    private boolean success;
    private List<String> validationErrors;
    private String error;
    private String[] contraIndicators;
    private String identityCheckScore;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String[] getContraIndicators() {
        return contraIndicators;
    }

    public void setContraIndicators(String[] contraIndicators) {
        this.contraIndicators = contraIndicators;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getIdentityCheckScore() {
        return identityCheckScore;
    }

    public void setIdentityCheckScore(String identityCheckScore) {
        this.identityCheckScore = identityCheckScore;
    }
}
