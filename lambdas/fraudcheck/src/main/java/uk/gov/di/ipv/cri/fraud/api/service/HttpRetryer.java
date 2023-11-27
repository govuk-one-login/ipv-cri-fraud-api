package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.util.HTTPReply;
import uk.gov.di.ipv.cri.fraud.api.util.HTTPReplyHelper;
import uk.gov.di.ipv.cri.fraud.api.util.SleepHelper;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class HttpRetryer {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final long HTTP_RETRY_WAIT_TIME_LIMIT_MS = 12800L;

    private final SleepHelper sleepHelper;
    private final CloseableHttpClient httpClient;

    private final int maxRetries;

    private final EventProbe eventProbe;

    public HttpRetryer(CloseableHttpClient httpClient, EventProbe eventProbe, int maxRetries) {
        this.sleepHelper = new SleepHelper(HTTP_RETRY_WAIT_TIME_LIMIT_MS);
        this.httpClient = httpClient;
        this.eventProbe = eventProbe;
        this.maxRetries = maxRetries;

        LOGGER.info("Max retries configured as {}", maxRetries);
    }

    public HTTPReply sendHTTPRequestRetryIfAllowed(
            HttpUriRequest request,
            HttpRetryStatusConfig httpRetryStatusConfig,
            String endpointName)
            throws IOException, OAuthErrorResponseException {

        // 0 is initial request, > 0 are retries
        int tryCount = 0;
        boolean retry = false;
        int statusCode = 0;
        HTTPReply reply = null;

        do {
            if (retry) {
                eventProbe.counterMetric(httpRetryStatusConfig.httpRetryerSendRetryMetric());
            }

            // Wait before sending request (0ms for first try)
            sleepHelper.busyWaitWithExponentialBackOff(tryCount);

            try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {

                statusCode = httpResponse.getStatusLine().getStatusCode();
                retry = httpRetryStatusConfig.shouldHttpClientRetry(statusCode);

                reply = HTTPReplyHelper.retrieveResponse(httpResponse, endpointName);

                if (retry) {
                    LOGGER.warn("shouldHttpClientRetry statusCode - {}", statusCode);
                }

                LOGGER.info(
                        "HTTPRequestRetry - totalRequests {}, retries {}, retryNeeded {}, statusCode {}",
                        tryCount + 1,
                        tryCount,
                        retry,
                        statusCode);

            } catch (IOException e) {
                // Logic is "Not any of"
                if (!((e instanceof ConnectTimeoutException)
                        || (e instanceof SocketTimeoutException))) {
                    // Only above exceptions retried, All other IOExceptions are not
                    LOGGER.warn(
                            "Failure when executing http request - {} reason {}",
                            e.getClass().getCanonicalName(),
                            e.getMessage());
                    eventProbe.counterMetric(httpRetryStatusConfig.httpRetryerSendFailMetric(e));

                    throw e;
                }

                // For retries (tryCount>0) we want to rethrow only the last Exception
                if (tryCount < maxRetries) {

                    LOGGER.info(
                            "HTTPRequestRetry {} - totalRequests {}, retries {}, retrying {}",
                            e.getMessage(),
                            tryCount + 1,
                            tryCount,
                            true);

                    retry = true;
                } else {

                    LOGGER.info(
                            "HTTPRequestRetry {} - totalRequests {}, retries {}, retrying {}",
                            e.getMessage(),
                            tryCount + 1,
                            tryCount,
                            false);

                    LOGGER.warn(
                            "Failure when executing http request - {} reason {}",
                            e.getClass().getCanonicalName(),
                            e.getMessage());
                    eventProbe.counterMetric(httpRetryStatusConfig.httpRetryerSendFailMetric(e));

                    throw e;
                }
            }
        } while (retry && (tryCount++ < maxRetries));

        LOGGER.info("HTTPRequestRetry Exited lastStatusCode {}", statusCode);

        if (httpRetryStatusConfig.isSuccessStatusCode(statusCode)) {
            eventProbe.counterMetric(httpRetryStatusConfig.httpRetryerSendOkMetric());
        } else if (tryCount < maxRetries) {
            // Reachable when the remote api responds initially with a retryable status code, then
            // during a retry with a non-retryable status code.
            eventProbe.counterMetric(httpRetryStatusConfig.httpRetryerSendErrorMetric());
        } else {
            eventProbe.counterMetric(httpRetryStatusConfig.httpRetryerMaxRetriesMetric());
        }

        return reply;
    }
}
