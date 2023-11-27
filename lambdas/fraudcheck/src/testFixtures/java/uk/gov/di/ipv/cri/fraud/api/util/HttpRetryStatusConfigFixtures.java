package uk.gov.di.ipv.cri.fraud.api.util;

import uk.gov.di.ipv.cri.fraud.api.service.HttpRetryStatusConfig;

import java.util.List;

public class HttpRetryStatusConfigFixtures {
    private HttpRetryStatusConfigFixtures() {
        throw new IllegalStateException("Test Fixtures");
    }

    public static final String TEST_HTTP_RETRYER_SEND_OK_METRIC =
            "test_http_retryer_send_ok_metric";
    public static final String TEST_HTTP_RETRYER_SEND_FAIL_METRIC =
            "test_http_retryer_send_fail_metric";
    public static final String TEST_HTTP_RETRYER_SEND_ERROR_METRIC =
            "test_http_retryer_send_error_metric";
    public static final String TEST_HTTP_RETRYER_SEND_RETRY_METRIC =
            "test_http_retryer_send_retry_metric";
    public static final String TEST_HTTP_RETRYER_SEND_MAX_RETRIES_METRIC =
            "test_http_retryer_send_max_retries_metric";

    public static HttpRetryStatusConfig generateTestReplyStatusConfig(
            List<Integer> retryStatusCodes, List<Integer> successStatusCodes) {
        return new HttpRetryStatusConfig() {
            @Override
            public boolean shouldHttpClientRetry(int statusCode) {
                return retryStatusCodes.contains(statusCode);
            }

            @Override
            public boolean isSuccessStatusCode(int statusCode) {
                return successStatusCodes.contains(statusCode);
            }

            @Override
            public String httpRetryerSendOkMetric() {
                return TEST_HTTP_RETRYER_SEND_OK_METRIC;
            }

            @Override
            public String httpRetryerSendFailMetric(Exception e) {
                return TEST_HTTP_RETRYER_SEND_FAIL_METRIC;
            }

            @Override
            public String httpRetryerSendErrorMetric() {
                return TEST_HTTP_RETRYER_SEND_ERROR_METRIC;
            }

            @Override
            public String httpRetryerSendRetryMetric() {
                return TEST_HTTP_RETRYER_SEND_RETRY_METRIC;
            }

            @Override
            public String httpRetryerMaxRetriesMetric() {
                return TEST_HTTP_RETRYER_SEND_MAX_RETRIES_METRIC;
            }
        };
    }
}
