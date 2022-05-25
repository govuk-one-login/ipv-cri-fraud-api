package uk.gov.di.ipv.cri.fraud.api.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HmacGeneratorTest {

    private HmacGenerator hmacGenerator;

    @BeforeEach
    void setup() throws Exception {
        hmacGenerator = new HmacGenerator("experian-secret-key");
    }

    @Test
    void shouldReturnHMACEncodedBase64String() {
        assertNotNull(hmacGenerator.generateHmac("{json:requestPayload}"));
    }

    @Test
    void constructorShouldThrowIllegalArgumentExceptionWhenNullInputProvided() {
        Arrays.stream(new String[] {null, "", "  "})
                .forEach(
                        hmacKey -> {
                            IllegalArgumentException throwException =
                                    assertThrows(
                                            IllegalArgumentException.class,
                                            () -> new HmacGenerator(hmacKey));
                            assertEquals("hmacKey must be specified", throwException.getMessage());
                        });
    }

    @Test
    void shouldThrowExceptionWhenPayloadIsNull() {
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> hmacGenerator.generateHmac(null));
        assertEquals("The input must not be null", exception.getMessage());
    }
}
