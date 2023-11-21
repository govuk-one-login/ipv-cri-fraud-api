package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.util.HttpResponseFixtures;
import uk.gov.di.ipv.cri.fraud.api.util.HttpRetryStatusConfigFixtures;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpRetryerTest {

    @Mock private CloseableHttpClient mockHttpClient;
    @Mock private EventProbe mockEventProbe;

    private HttpRetryer httpRetryer;
    @Mock private HttpPost mockPostRequest;

    private List<Integer> TEST_RETRY_STATUS_CODES = List.of(300, 400, 500);
    private List<Integer> TEST_SUCCESS_STATUS_CODES = List.of(200, 201);
    private int TEST_MAX_RETRIES = 3;

    @BeforeEach
    void setUp() {
        httpRetryer = new HttpRetryer(mockHttpClient, mockEventProbe, TEST_MAX_RETRIES);
    }

    @ParameterizedTest
    @CsvSource({
        "200, false", // No Retry
        "201, false", // No Retry
        "300, true", // Retry Expected
        "400, true", // Retry Expected
        "500, true", // Retry Expected
    })
    void shouldOnlyRetryWhenStatusIsAMatchingRetryCode(int statusCode, boolean retryExpected)
            throws IOException {

        CloseableHttpResponse testCloseableHttpResponse =
                HttpResponseFixtures.createHttpResponse(statusCode, null, "", false);

        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(testCloseableHttpResponse);

        HttpRetryStatusConfig testHttpRetryStatusConfig =
                HttpRetryStatusConfigFixtures.generateTestReplyStatusConfig(
                        TEST_RETRY_STATUS_CODES, TEST_SUCCESS_STATUS_CODES);
        httpRetryer.sendHTTPRequestRetryIfAllowed(mockPostRequest, testHttpRetryStatusConfig);

        int mockHttpClientExpectedTimes = 1; // 1 for the initial attempt
        if (retryExpected) {
            mockHttpClientExpectedTimes += TEST_MAX_RETRIES;

            InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
            inOrderMockEventProbeSequence
                    .verify(mockEventProbe, times(TEST_MAX_RETRIES))
                    .counterMetric(
                            HttpRetryStatusConfigFixtures.TEST_HTTP_RETRYER_SEND_RETRY_METRIC);
            inOrderMockEventProbeSequence
                    .verify(mockEventProbe, times(1))
                    .counterMetric(
                            HttpRetryStatusConfigFixtures
                                    .TEST_HTTP_RETRYER_SEND_MAX_RETRIES_METRIC);
        } else {
            // Send Success
            verify(mockEventProbe)
                    .counterMetric(HttpRetryStatusConfigFixtures.TEST_HTTP_RETRYER_SEND_OK_METRIC);
        }
        verifyNoMoreInteractions(mockEventProbe);

        verify(mockHttpClient, times(mockHttpClientExpectedTimes)).execute(any(HttpPost.class));
        verifyNoMoreInteractions(mockHttpClient);
    }

    @ParameterizedTest
    @CsvSource({
        "IOException, false", // No Retry
        "ConnectTimeoutException, true", // Retry Expected
        "SocketTimeoutException, true", // Retry Expected
    })
    void shouldOnlyRetryWhenIOExceptionIsARetryableException(
            String exceptionToThrow, boolean retryExpected) throws IOException {

        final Exception exception;

        if (exceptionToThrow.equals("ConnectTimeoutException")) {
            exception = new ConnectTimeoutException("TestConnectTimeoutException");
        } else if (exceptionToThrow.equals("SocketTimeoutException")) {
            exception = new SocketTimeoutException("TestSocketTimeoutException");
        } else {
            exception = new IOException("TestGeneralIOException");
        }

        when(mockHttpClient.execute(any(HttpPost.class))).thenThrow(exception);

        HttpRetryStatusConfig testHttpRetryStatusConfig =
                HttpRetryStatusConfigFixtures.generateTestReplyStatusConfig(
                        TEST_RETRY_STATUS_CODES, TEST_SUCCESS_STATUS_CODES);

        IOException thrownException =
                assertThrows(
                        IOException.class,
                        () ->
                                httpRetryer.sendHTTPRequestRetryIfAllowed(
                                        mockPostRequest, testHttpRetryStatusConfig),
                        "Expected IOException");

        int mockHttpClientExpectedTimes = 1; // The initial attempt
        if (retryExpected) {
            mockHttpClientExpectedTimes += TEST_MAX_RETRIES;

            InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
            // x3 retries
            inOrderMockEventProbeSequence
                    .verify(mockEventProbe, times(TEST_MAX_RETRIES))
                    .counterMetric(
                            HttpRetryStatusConfigFixtures.TEST_HTTP_RETRYER_SEND_RETRY_METRIC);
            inOrderMockEventProbeSequence
                    .verify(mockEventProbe)
                    .counterMetric(
                            HttpRetryStatusConfigFixtures.TEST_HTTP_RETRYER_SEND_FAIL_METRIC);

            // No retry after final attempt, Exception rethrown
            if (exceptionToThrow.equals("ConnectTimeoutException")) {
                assertTrue(thrownException instanceof ConnectTimeoutException);
            } else if (exceptionToThrow.equals("SocketTimeoutException")) {
                assertTrue(thrownException instanceof SocketTimeoutException);
            }
        } else {
            // Send Fail - IOException
            verify(mockEventProbe)
                    .counterMetric(
                            HttpRetryStatusConfigFixtures.TEST_HTTP_RETRYER_SEND_FAIL_METRIC);
            assertTrue(thrownException instanceof IOException);
        }
        verifyNoMoreInteractions(mockEventProbe);

        verify(mockHttpClient, times(mockHttpClientExpectedTimes)).execute(any(HttpPost.class));
        verifyNoMoreInteractions(mockHttpClient);
    }

    @Test
    void shouldCaptureSendErrorMetricIfRemoteAPIReturnsNonRetryableStatusDuringARetry()
            throws IOException {

        // Response with retryable status code
        CloseableHttpResponse initialRetryableCloseableHttpResponse =
                HttpResponseFixtures.createHttpResponse(500, null, "", false);

        // 999 status is not in the success list or in the retry list
        CloseableHttpResponse nonRetryableCloseableHttpResponse =
                HttpResponseFixtures.createHttpResponse(999, null, "", false);

        // x2 returns fro the above
        when(mockHttpClient.execute(any(HttpPost.class)))
                .thenReturn(initialRetryableCloseableHttpResponse)
                .thenReturn(nonRetryableCloseableHttpResponse);

        HttpRetryStatusConfig testHttpRetryStatusConfig =
                HttpRetryStatusConfigFixtures.generateTestReplyStatusConfig(
                        TEST_RETRY_STATUS_CODES, TEST_SUCCESS_STATUS_CODES);
        httpRetryer.sendHTTPRequestRetryIfAllowed(mockPostRequest, testHttpRetryStatusConfig);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(HttpRetryStatusConfigFixtures.TEST_HTTP_RETRYER_SEND_RETRY_METRIC);

        // Send Error
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(HttpRetryStatusConfigFixtures.TEST_HTTP_RETRYER_SEND_ERROR_METRIC);

        verifyNoMoreInteractions(mockEventProbe);

        verify(mockHttpClient, times(2)).execute(any(HttpPost.class));
        verifyNoMoreInteractions(mockHttpClient);
    }
}
