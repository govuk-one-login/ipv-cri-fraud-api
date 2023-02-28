package uk.gov.di.ipv.cri.fraud.api.domain;

public class FraudCheckResult {
    private boolean executedSuccessfully;
    private String[] thirdPartyFraudCodes;
    private String errorMessage;
    private String transactionId;
    private String decisionScore;
    private String oldestRecordDate;

    public FraudCheckResult() {
        this.thirdPartyFraudCodes = new String[] {};
    }

    public boolean isExecutedSuccessfully() {
        return executedSuccessfully;
    }

    public void setExecutedSuccessfully(boolean executedSuccessfully) {
        this.executedSuccessfully = executedSuccessfully;
    }

    public String[] getThirdPartyFraudCodes() {
        return thirdPartyFraudCodes;
    }

    public void setThirdPartyFraudCodes(String[] thirdPartyFraudCodes) {
        this.thirdPartyFraudCodes = thirdPartyFraudCodes;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getDecisionScore() {
        return decisionScore;
    }

    public void setDecisionScore(String decisionScore) {
        this.decisionScore = decisionScore;
    }

    public String getOldestRecordDate() {
        return oldestRecordDate;
    }

    public void setOldestRecordDate(String oldestRecordDate) {
        this.oldestRecordDate = oldestRecordDate;
    }
}
