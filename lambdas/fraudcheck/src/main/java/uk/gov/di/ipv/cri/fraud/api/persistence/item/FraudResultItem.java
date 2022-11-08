package uk.gov.di.ipv.cri.fraud.api.persistence.item;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.List;
import java.util.UUID;

@DynamoDbBean
public class FraudResultItem {
    private UUID sessionId;
    private List<String> contraIndicators;
    private Integer identityFraudScore;
    private String transactionId;
    private String decisionScore;

    public FraudResultItem() {}

    public FraudResultItem(
            UUID sessionId,
            List<String> contraIndicators,
            Integer identityFraudScore,
            String decisionScore) {
        this.sessionId = sessionId;
        this.contraIndicators = contraIndicators;
        this.identityFraudScore = identityFraudScore;
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

    public String getDecisionScore() {
        return decisionScore;
    }

    public void setDecisionScore(String decisionScore) {
        this.decisionScore = decisionScore;
    }
}
