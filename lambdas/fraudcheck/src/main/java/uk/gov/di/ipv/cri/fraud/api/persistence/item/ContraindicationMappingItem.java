package uk.gov.di.ipv.cri.fraud.api.persistence.item;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class ContraindicationMappingItem {
    private String thirdPartyId;
    private String thirdPartyFraudCode;
    private String contraindicationCode;

    @DynamoDbSortKey
    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }

    @DynamoDbPartitionKey
    public String getThirdPartyFraudCode() {
        return thirdPartyFraudCode;
    }

    public void setThirdPartyFraudCode(String thirdPartyFraudCode) {
        this.thirdPartyFraudCode = thirdPartyFraudCode;
    }

    public String getContraindicationCode() {
        return contraindicationCode;
    }

    public void setContraindicationCode(String contraindicationCode) {
        this.contraindicationCode = contraindicationCode;
    }
}
