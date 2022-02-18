package uk.gov.di.ipv.cri.fraud.library.persistance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import uk.gov.di.ipv.cri.fraud.library.persistence.DataStore;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.AccessTokenItem;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataStoreTest {
    private static final String TEST_TABLE_NAME = "test-auth-code-table";

    @Mock private DynamoDbEnhancedClient mockDynamoDbEnhancedClient;
    @Mock private DynamoDbTable<AccessTokenItem> mockDynamoDbTable;

    private AccessTokenItem accessTokenItem;
    private DataStore<AccessTokenItem> dataStore;

    @BeforeEach
    void setUp() {
        when(mockDynamoDbEnhancedClient.table(
                        anyString(), ArgumentMatchers.<TableSchema<AccessTokenItem>>any()))
                .thenReturn(mockDynamoDbTable);

        accessTokenItem = new AccessTokenItem();
        String accessToken = UUID.randomUUID().toString();
        accessTokenItem.setAccessToken(accessToken);
        accessTokenItem.setResourceId("test-resource-12345");

        dataStore =
                new DataStore<>(TEST_TABLE_NAME, AccessTokenItem.class, mockDynamoDbEnhancedClient);
    }

    @Test
    void shouldPutItemIntoDynamoDbTable() {
        dataStore.create(accessTokenItem);

        ArgumentCaptor<AccessTokenItem> authorizationCodeItemArgumentCaptor =
                ArgumentCaptor.forClass(AccessTokenItem.class);

        verify(mockDynamoDbEnhancedClient)
                .table(eq(TEST_TABLE_NAME), ArgumentMatchers.<TableSchema<AccessTokenItem>>any());
        verify(mockDynamoDbTable).putItem(authorizationCodeItemArgumentCaptor.capture());
        assertEquals(
                accessTokenItem.getAccessToken(),
                authorizationCodeItemArgumentCaptor.getValue().getAccessToken());
        assertEquals(
                accessTokenItem.getResourceId(),
                authorizationCodeItemArgumentCaptor.getValue().getResourceId());
    }

    @Test
    void shouldGetItemFromDynamoDbTableViaPartitionKeyAndSortKey() {
        dataStore.getItem("partition-key-12345", "sort-key-12345");

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);

        verify(mockDynamoDbEnhancedClient)
                .table(eq(TEST_TABLE_NAME), ArgumentMatchers.<TableSchema<AccessTokenItem>>any());
        verify(mockDynamoDbTable).getItem(keyCaptor.capture());
        assertEquals("partition-key-12345", keyCaptor.getValue().partitionKeyValue().s());
        assertEquals("sort-key-12345", keyCaptor.getValue().sortKeyValue().get().s());
    }

    @Test
    void shouldGetItemFromDynamoDbTableViaPartitionKey() {
        dataStore.getItem("partition-key-12345");

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);

        verify(mockDynamoDbEnhancedClient)
                .table(eq(TEST_TABLE_NAME), ArgumentMatchers.<TableSchema<AccessTokenItem>>any());
        verify(mockDynamoDbTable).getItem(keyCaptor.capture());
        assertEquals("partition-key-12345", keyCaptor.getValue().partitionKeyValue().s());
        assertTrue(keyCaptor.getValue().sortKeyValue().isEmpty());
    }

    @Test
    void shouldDeleteItemFromDynamoDbTableViaPartitionKeyAndSortKey() {
        dataStore.delete("partition-key-12345", "sort-key-12345");

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);

        verify(mockDynamoDbEnhancedClient)
                .table(eq(TEST_TABLE_NAME), ArgumentMatchers.<TableSchema<AccessTokenItem>>any());
        verify(mockDynamoDbTable).deleteItem(keyCaptor.capture());
        assertEquals("partition-key-12345", keyCaptor.getValue().partitionKeyValue().s());
        assertEquals("sort-key-12345", keyCaptor.getValue().sortKeyValue().get().s());
    }

    @Test
    void shouldDeleteItemFromDynamoDbTableViaPartitionKey() {
        dataStore.delete("partition-key-12345");

        ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);

        verify(mockDynamoDbEnhancedClient)
                .table(eq(TEST_TABLE_NAME), ArgumentMatchers.<TableSchema<AccessTokenItem>>any());
        verify(mockDynamoDbTable).deleteItem(keyCaptor.capture());
        assertEquals("partition-key-12345", keyCaptor.getValue().partitionKeyValue().s());
        assertTrue(keyCaptor.getValue().sortKeyValue().isEmpty());
    }
}
