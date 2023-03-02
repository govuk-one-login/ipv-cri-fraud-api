package uk.gov.di.ipv.cri.fraud.library.persistence.item;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.List;
import java.util.UUID;

@DynamoDbBean
public class FraudResultItem {
    private UUID sessionId;
    private List<String> contraIndicators;
    private Integer identityFraudScore;
    private Integer activityHistoryScore;
    private String transactionId;
    private String pepTransactionId;
    private String decisionScore;

    private List<String> checkDetails;
    private List<String> failedCheckDetails;

    public FraudResultItem() {}

    public FraudResultItem(
            UUID sessionId,
            List<String> contraIndicators,
            Integer identityFraudScore,
            Integer activityHistoryScore,
            String decisionScore) {
        this.sessionId = sessionId;
        this.contraIndicators = contraIndicators;
        this.identityFraudScore = identityFraudScore;
        this.activityHistoryScore = activityHistoryScore;
        this.decisionScore = decisionScore;
    }

    @DynamoDbPartitionKey()
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public List<String> getContraIndicators() {
        return contraIndicators;
    }

    public void setContraIndicators(List<String> contraIndicators) {
        this.contraIndicators = contraIndicators;
    }

    public Integer getIdentityFraudScore() {
        return identityFraudScore;
    }

    public void setIdentityFraudScore(Integer identityFraudScore) {
        this.identityFraudScore = identityFraudScore;
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

    public List<String> getCheckDetails() {
        return checkDetails;
    }

    public void setCheckDetails(List<String> checkDetails) {
        this.checkDetails = checkDetails;
    }

    public List<String> getFailedCheckDetails() {
        return failedCheckDetails;
    }

    public void setFailedCheckDetails(List<String> failedCheckDetails) {
        this.failedCheckDetails = failedCheckDetails;
    }

    public Integer getActivityHistoryScore() {
        return activityHistoryScore;
    }

    public void setActivityHistoryScore(Integer activityHistoryScore) {
        this.activityHistoryScore = activityHistoryScore;
    }
}
