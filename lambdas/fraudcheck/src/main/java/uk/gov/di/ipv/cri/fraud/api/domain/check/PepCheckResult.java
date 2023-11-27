package uk.gov.di.ipv.cri.fraud.api.domain.check;

public class PepCheckResult {
    private boolean executedSuccessfully;
    private String[] thirdPartyFraudCodes;
    private String errorMessage;
    private String transactionId;

    public PepCheckResult() {
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
}
