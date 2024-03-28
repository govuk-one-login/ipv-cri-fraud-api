package uk.gov.di.ipv.cri.fraud.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCheckResultStorageServiceTest {

    @Mock ClientFactoryService mockClientFactoryService;

    @Mock DynamoDbEnhancedClient mockDynamoDbEnhancedClient;
    @Mock DynamoDbTable<FraudResultItem> mockDynamoDbTable;

    private ResultItemStorageService<FraudResultItem> resultItemStorageService;

    @BeforeEach
    @java.lang.SuppressWarnings("java:S6068")
    void setUp() {
        when(mockClientFactoryService.getDynamoDbEnhancedClient())
                .thenReturn(mockDynamoDbEnhancedClient);

        // Ignore sonar eq is required for this (S6068)
        when(mockDynamoDbEnhancedClient.table(
                        eq("TestTable"), eq(TableSchema.fromBean(FraudResultItem.class))))
                .thenReturn(mockDynamoDbTable);

        resultItemStorageService =
                new ResultItemStorageService<>(
                        "TestTable", FraudResultItem.class, mockClientFactoryService);
    }

    @Test
    void shouldSaveDocumentCheckResultItem() {
        assertDoesNotThrow(() -> resultItemStorageService.saveResultItem(new FraudResultItem()));
    }

    @Test
    void shouldGetDocumentCheckResult() {

        UUID testUUID = UUID.randomUUID();

        FraudResultItem testItem = new FraudResultItem();

        when(mockDynamoDbTable.getItem(Key.builder().partitionValue(testUUID.toString()).build()))
                .thenReturn(testItem);

        FraudResultItem returnedItem =
                assertDoesNotThrow(() -> resultItemStorageService.getResultItem(testUUID));

        assertEquals(testItem, returnedItem);
    }
}
