package uk.gov.di.ipv.cri.fraud.library.helpers;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestHelperTest {

    @Test
    void getHeaderByKeyShouldReturnHeaderIfMatchFound() {
        assertEquals("toyou", RequestHelper.getHeaderByKey(Map.of("tome", "toyou"), "tome"));
    }

    @Test
    void getHeaderByKeyShouldReturnNullIfHeaderNotFound() {
        assertNull(RequestHelper.getHeaderByKey(Map.of("tome", "toyou"), "ohdearohdear"));
    }

    @Test
    void getHeaderByKeyShouldReturnNullIfNoHeadersProvided() {
        assertNull(RequestHelper.getHeaderByKey(null, "ohdearohdear"));
    }
}
