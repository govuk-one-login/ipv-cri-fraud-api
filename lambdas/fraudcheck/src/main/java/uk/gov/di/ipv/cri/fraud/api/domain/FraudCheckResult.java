package uk.gov.di.ipv.cri.fraud.api.domain;

@DynamoDbBean
public class FraudCheckResult {
    private boolean executedSuccessfully;
    private String[] thirdPartyFraudCodes;
    private String errorMessage;
    private UUID sessionId;
    private String[] contraIndicators;
    private String identityCheckScore;

    @DynamoDbPartitionKey()
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public FraudCheckResult() {
        this.thirdPartyFraudCodes = new String[] {};
        this.identityCheckScore = "0";
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

    public String[] getContraIndicators() {
        return contraIndicators;
    }

    public void setContraIndicators(String[] contraIndicators) {
        this.contraIndicators = contraIndicators;
    }

    public String getIdentityCheckScore() {
        return identityCheckScore;
    }

    public void setIdentityCheckScore(String identityCheckScore) {
        this.identityCheckScore = identityCheckScore;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
