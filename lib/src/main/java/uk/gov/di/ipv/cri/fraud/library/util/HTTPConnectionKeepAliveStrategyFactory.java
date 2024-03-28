package uk.gov.di.ipv.cri.fraud.library.util;

import lombok.experimental.UtilityClass;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@UtilityClass
public class HTTPConnectionKeepAliveStrategyFactory {
    private static final Logger LOGGER = LogManager.getLogger();

    // See https://hc.apache.org/httpcomponents-client-4.5.x/current/tutorial/html/connmgmt.html
    public static ConnectionKeepAliveStrategy createHTTPConnectionKeepAliveStrategy(
            long keepAliveSeconds, boolean useRemoteHeaderValue) {

        return (response, context) -> {
            long requestedKeepAlive = keepAliveSeconds;

            if (useRemoteHeaderValue) {

                // Will used the remote value if present or fallback to keepAliveSeconds
                requestedKeepAlive =
                        retrieveRemoteHeaderKeepAliveHeaderIfPresent(keepAliveSeconds, response);
            }

            // Use our requestedKeepAlive if sensible, else use a default
            long keepAliveMS = requestedKeepAlive > 0 ? (requestedKeepAlive * 1000) : (30 * 1000);

            LOGGER.info("Using Keep-Alive of {}ms", keepAliveMS);

            return keepAliveMS;
        };
    }

    // Honor 'keep-alive' header if present
    private static long retrieveRemoteHeaderKeepAliveHeaderIfPresent(
            long fallbackKeepAliveSeconds, HttpResponse response) {
        HeaderElementIterator it =
                new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));

        while (it.hasNext()) {
            HeaderElement headerElement = it.nextElement();
            String headerParam = headerElement.getName();
            String headerValue = headerElement.getValue();
            if (headerValue != null && headerParam.equalsIgnoreCase("timeout")) {
                try {
                    // Use the remote header values
                    long remoteValueSeconds = Long.parseLong(headerValue);

                    LOGGER.info(
                            "Remote Header has timeout present with value of {} seconds",
                            remoteValueSeconds);

                    return remoteValueSeconds;
                } catch (NumberFormatException ignore) {
                    // Junk in the header - Do nothing with exception
                    // Fall through to our fallbackKeepAliveSeconds
                }
            }
        }

        return fallbackKeepAliveSeconds;
    }
}
