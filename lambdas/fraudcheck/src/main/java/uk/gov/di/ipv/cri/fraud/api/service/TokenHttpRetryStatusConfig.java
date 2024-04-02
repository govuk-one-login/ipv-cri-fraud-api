package uk.gov.di.ipv.cri.fraud.api.service;

import uk.gov.di.ipv.cri.fraud.library.service.HttpRetryStatusConfig;

import java.util.List;

import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_HTTP_RETRYER_REQUEST_SEND_FAIL;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_HTTP_RETRYER_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_HTTP_RETRYER_REQUEST_SEND_RETRY;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_HTTP_RETRYER_SEND_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_HTTP_RETRYER_SEND_MAX_RETRIES;

public class TokenHttpRetryStatusConfig implements HttpRetryStatusConfig {

    private final List<Integer> neverRetryCodes = List.of(200, 400, 401, 403);

    private final List<Integer> retryCodes = List.of(429);

    private final List<Integer> successStatusCodes = List.of(200);

    @Override
    public boolean shouldHttpClientRetry(int statusCode) {

        // Status codes where retrying is not valid
        if (neverRetryCodes.contains(statusCode)) {
            return false;
        }

        // Retry all server errors
        if (isServerErrorStatusCode(statusCode)) {
            return true;
        }

        // Retry status codes - all others are no retry
        return retryCodes.contains(statusCode);
    }

    @Override
    public boolean isSuccessStatusCode(int statusCode) {
        return successStatusCodes.contains(statusCode);
    }

    private boolean isServerErrorStatusCode(int statusCode) {
        return (statusCode >= 500) && (statusCode <= 599);
    }

    @Override
    public String httpRetryerSendOkMetric() {
        return TOKEN_HTTP_RETRYER_REQUEST_SEND_OK.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendFailMetric(Exception e) {
        return TOKEN_HTTP_RETRYER_REQUEST_SEND_FAIL.withEndpointPrefixAndExceptionName(e);
    }

    @Override
    public String httpRetryerSendErrorMetric() {
        return TOKEN_HTTP_RETRYER_SEND_ERROR.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendRetryMetric() {
        return TOKEN_HTTP_RETRYER_REQUEST_SEND_RETRY.withEndpointPrefix();
    }

    @Override
    public String httpRetryerMaxRetriesMetric() {
        return TOKEN_HTTP_RETRYER_SEND_MAX_RETRIES.withEndpointPrefix();
    }
}
