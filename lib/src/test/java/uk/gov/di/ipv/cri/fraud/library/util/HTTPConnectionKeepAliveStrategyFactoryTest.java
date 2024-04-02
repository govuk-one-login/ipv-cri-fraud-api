package uk.gov.di.ipv.cri.fraud.library.util;

import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.di.ipv.cri.fraud.library.HttpResponseFixtures;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HTTPConnectionKeepAliveStrategyFactoryTest {

    @ParameterizedTest
    @CsvSource({"30, false", "1000, true"})
    void shouldCreateHTTPConnectionKeepAliveStrategy(
            long keepAliveSeconds, boolean useRemoteHeader) {
        Map<String, String> testHeaders = new HashMap<>();

        ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
                HTTPConnectionKeepAliveStrategyFactory.createHTTPConnectionKeepAliveStrategy(
                        keepAliveSeconds, useRemoteHeader);

        long expectedkeepAliveSeconds = keepAliveSeconds;

        if (useRemoteHeader) {
            expectedkeepAliveSeconds = 50;
            testHeaders.put("Keep-Alive", "timeout=" + String.valueOf(expectedkeepAliveSeconds));
        }

        long actualKeepAliveMs =
                connectionKeepAliveStrategy.getKeepAliveDuration(
                        HttpResponseFixtures.createHttpResponse(200, testHeaders, "", false), null);
        long actualKeepAliveSeconds = actualKeepAliveMs / 1000;

        assertEquals(expectedkeepAliveSeconds, actualKeepAliveSeconds);
    }
}
