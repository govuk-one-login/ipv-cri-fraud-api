package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContraIndicatorMapperTest {

    private static final String TEST_UCODE_1 = "Z101";
    private static final String TEST_CIMAP_1 = TEST_UCODE_1 + ":A1";

    private static final String TEST_UCODE_2 = "Z102";
    private static final String TEST_CIMAP_2 = TEST_UCODE_2 + ":A2";

    private static final String TEST_UCODE_3 = "Z103";
    private static final String TEST_UCODE_4 = "Z104";
    private static final String TEST_UCODE_5 = "Z105";
    private static final String TEST_UCODES_3 =
            TEST_UCODE_3 + "," + TEST_UCODE_4 + "," + TEST_UCODE_5;
    private static final String TEST_CIMAP_3 = TEST_UCODES_3 + ":A3";

    // The full test mapping string
    private static final String TEST_CIMAP_STRING =
            TEST_CIMAP_1 + "||" + TEST_CIMAP_2 + "||" + TEST_CIMAP_3;

    private static ContraIndicatorMapper envVarMapper;

    @BeforeAll
    public static void Setup() {
        envVarMapper = new ContraIndicatorMapper(TEST_CIMAP_STRING);
    }

    @Test
    @DisplayName("Mapping the CI's from the Environment Variable.")
    void ContraIndicatorEnvVarMapperMappingTest() {
        assertNotNull(envVarMapper);
    }

    @Test
    @DisplayName("Read a Mapping")
    void CheckSingleCodeMapping() {
        String[] ciCode = envVarMapper.mapThirdPartyFraudCodes(new String[] {TEST_UCODE_1});

        assertEquals(1, ciCode.length);
    }

    @Test
    @DisplayName("Read a two mappings")
    void CheckMultiCodeMapping() {
        String[] ciCode =
                envVarMapper.mapThirdPartyFraudCodes(new String[] {TEST_UCODE_1, TEST_UCODE_2});

        assertEquals(2, ciCode.length);
    }

    @Test
    @DisplayName("Read a many to one mapping")
    void CheckManyToOneCodeMapping() {
        String[] ciCode =
                envVarMapper.mapThirdPartyFraudCodes(
                        new String[] {TEST_UCODE_3, TEST_UCODE_4, TEST_UCODE_5});

        assertEquals(1, ciCode.length);
    }

    @Test
    @DisplayName("Throws Exception with NULL lookup")
    void mapThirdPartyFraudCodesShouldThrowExceptionWhenNullInputProvided() {
        NullPointerException thrownException =
                assertThrows(
                        NullPointerException.class,
                        () -> envVarMapper.mapThirdPartyFraudCodes(null));
        assertNotNull(thrownException);
        assertEquals("thirdPartyFraudCodes must not be null", thrownException.getMessage());
    }

    @Test
    @DisplayName("Empty return when Empty provided.")
    void mapThirdPartyFraudCodesShouldReturnEmptyArrayWhenEmptyArrayInputProvided() {
        String[] result = envVarMapper.mapThirdPartyFraudCodes(new String[] {});

        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
