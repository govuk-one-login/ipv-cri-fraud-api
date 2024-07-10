package uk.gov.di.ipv.cri.fraud.library.service;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;

import java.util.UUID;

public class ResultItemStorageService<T> {

    private final DataStore<T> resultItemDataStore;

    public ResultItemStorageService(
            String resultItemTableName,
            Class<T> resultItemClass,
            DynamoDbEnhancedClient dynamoDbEnhancedClient) {

        this.resultItemDataStore =
                new DataStore<>(resultItemTableName, resultItemClass, dynamoDbEnhancedClient);
    }

    public T getResultItem(UUID sessionId) {
        return resultItemDataStore.getItem(sessionId.toString());
    }

    public void saveResultItem(T resultItem) {
        resultItemDataStore.create(resultItem);
    }
}
