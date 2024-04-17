package uk.gov.di.ipv.cri.fraud.dynamotest.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.util.UUID;

@DynamoDbBean
public class DBTestSessionItem {
    public static final String AUTHORIZATION_CODE_INDEX = "db-test-authorizationCode-index";
    public static final String ACCESS_TOKEN_INDEX = "db-test-access-token-index";
    private UUID sessionId;
    private long expiryDate;
    private String authorizationCode;
    private long authorizationCodeExpiryDate;
    private String accessToken;
    private long accessTokenExpiryDate;

    public DBTestSessionItem() {
        sessionId = UUID.randomUUID();
    }

    @DynamoDbPartitionKey()
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = AUTHORIZATION_CODE_INDEX)
    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = ACCESS_TOKEN_INDEX)
    public String getAccessToken() {
        return accessToken;
    }

    public long getAuthorizationCodeExpiryDate() {
        return authorizationCodeExpiryDate;
    }

    public void setAuthorizationCodeExpiryDate(long authorizationCodeExpiryDate) {
        this.authorizationCodeExpiryDate = authorizationCodeExpiryDate;
    }

    public long getAccessTokenExpiryDate() {
        return accessTokenExpiryDate;
    }

    public void setAccessTokenExpiryDate(long accessTokenExpiryDate) {
        this.accessTokenExpiryDate = accessTokenExpiryDate;
    }
}
