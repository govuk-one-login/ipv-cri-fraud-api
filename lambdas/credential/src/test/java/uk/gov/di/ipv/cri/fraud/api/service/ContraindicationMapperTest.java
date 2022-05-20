package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.api.persistence.item.ContraindicationMappingItem;
import uk.gov.di.ipv.cri.fraud.library.persistence.DataStore;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContraindicationMapperTest {
    private static final String TEST_THIRD_PARTY_ID = "third-party-id";
    private static final String THIRD_PARTY_CODE_ONE = "third-party-code-1";
    private static final String THIRD_PARTY_CODE_TWO = "third-party-code-2";
    private static final String CONTRAINDICATION_CODE_ONE = "contra-indication-code-1";
    private static final String CONTRAINDICATION_CODE_TWO = "contra-indication-code-2";

    @Mock private DataStore<ContraindicationMappingItem> mockDataStore;

    private ContraindicationMapper contraindicationMapper;

    @BeforeEach
    void setup() {
        ContraindicationMappingItem testMappingItemOne = new ContraindicationMappingItem();
        testMappingItemOne.setContraindicationCode(CONTRAINDICATION_CODE_ONE);
        testMappingItemOne.setThirdPartyFraudCode(THIRD_PARTY_CODE_ONE);

        ContraindicationMappingItem testMappingItemTwo = new ContraindicationMappingItem();
        testMappingItemTwo.setContraindicationCode(CONTRAINDICATION_CODE_TWO);
        testMappingItemTwo.setThirdPartyFraudCode(THIRD_PARTY_CODE_TWO);

        List<ContraindicationMappingItem> contraindicationMappingItems =
                List.of(testMappingItemOne, testMappingItemTwo);
        when(mockDataStore.getItemsByAttribute("thirdPartyId", TEST_THIRD_PARTY_ID))
                .thenReturn(contraindicationMappingItems);
        this.contraindicationMapper =
                new ContraindicationMapper(mockDataStore, TEST_THIRD_PARTY_ID);
    }

    @Test
    void mapThirdPartyFraudCodesShouldReturnMappedCodesWhenValidInputProvided() {
        String[] result =
                contraindicationMapper.mapThirdPartyFraudCodes(
                        new String[] {THIRD_PARTY_CODE_TWO, THIRD_PARTY_CODE_ONE});

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(
                2L,
                Arrays.stream(result)
                        .filter(
                                c ->
                                        c.equals(CONTRAINDICATION_CODE_TWO)
                                                || c.equals(CONTRAINDICATION_CODE_ONE))
                        .count());
    }

    @Test
    void mapThirdPartyFraudCodesShouldThrowExceptionWhenNullInputProvided() {
        NullPointerException thrownException =
                assertThrows(
                        NullPointerException.class,
                        () -> contraindicationMapper.mapThirdPartyFraudCodes(null));
        assertNotNull(thrownException);
        assertEquals("thirdPartyFraudCodes must not be null", thrownException.getMessage());
    }

    @Test
    void mapThirdPartyFraudCodesShouldReturnEmptyArrayWhenEmptyArrayInputProvided() {
        String[] result = contraindicationMapper.mapThirdPartyFraudCodes(new String[] {});

        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
