package uk.gov.di.ipv.cri.fraud.api.domain;

import java.util.List;

public class IdentityVerificationResult {
    private boolean success;
    private List<String> validationErrors;
    private String error;
    private List<String> contraIndicators;
    private int identityCheckScore;
    private String transactionId;
    private String pepTransactionId;
    private String decisionScore;

    // These checks have specific meanings and appear in the VC
    private List<String> checksSucceeded;
    private List<String> checksFailed;

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

    public List<String> getContraIndicators() {
        return contraIndicators;
    }

    public void setContraIndicators(List<String> contraIndicators) {
        this.contraIndicators = contraIndicators;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getIdentityCheckScore() {
        return identityCheckScore;
    }

    public void setIdentityCheckScore(int identityCheckScore) {
        this.identityCheckScore = identityCheckScore;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPepTransactionId() {
        return pepTransactionId;
    }

    public void setPepTransactionId(String pepTransactionId) {
        this.pepTransactionId = pepTransactionId;
    }

    public String getDecisionScore() {
        return decisionScore;
    }

    public void setDecisionScore(String decisionScore) {
        this.decisionScore = decisionScore;
    }

    public List<String> getChecksSucceeded() {
        return checksSucceeded;
    }

    public void setChecksSucceeded(List<String> checksSucceeded) {
        this.checksSucceeded = checksSucceeded;
    }

    public List<String> getChecksFailed() {
        return checksFailed;
    }

    public void setChecksFailed(List<String> checksFailed) {
        this.checksFailed = checksFailed;
    }
}
