package uk.gov.di.ipv.cri.fraud.api.domain;

public class FraudCheckResult {
    private boolean executedSuccessfully;
    private String[] thirdPartyFraudCodes;
    private String errorMessage;

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
}
