package uk.gov.di.ipv.cri.fraud.api.domain.check;

public class FraudCheckResult {
    private boolean executedSuccessfully;
    private String[] thirdPartyFraudCodes;
    private String errorMessage;
    private String transactionId;
    private Integer decisionScore;
    private Integer oldestRecordDateInMonths;

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

    public Integer getDecisionScore() {
        return decisionScore;
    }

    public void setDecisionScore(Integer decisionScore) {
        this.decisionScore = decisionScore;
    }

    public Integer getOldestRecordDateInMonths() {
        return oldestRecordDateInMonths;
    }

    public void setOldestRecordDateInMonths(Integer oldestRecordDateInMonths) {
        this.oldestRecordDateInMonths = oldestRecordDateInMonths;
    }
}
