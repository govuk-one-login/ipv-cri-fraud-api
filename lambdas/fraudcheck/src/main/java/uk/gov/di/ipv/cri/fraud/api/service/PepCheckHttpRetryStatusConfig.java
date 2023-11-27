package uk.gov.di.ipv.cri.fraud.api.service;

import java.util.List;

import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_HTTP_RETRYER_REQUEST_SEND_FAIL;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_HTTP_RETRYER_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_HTTP_RETRYER_REQUEST_SEND_RETRY;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_HTTP_RETRYER_SEND_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_HTTP_RETRYER_SEND_MAX_RETRIES;

public class PepCheckHttpRetryStatusConfig implements HttpRetryStatusConfig {

    private final List<Integer> neverRetryCodes = List.of(200);

    private final List<Integer> retryCodes = List.of(429);

    private final List<Integer> successStatusCodes = List.of(200);

    @Override
    public boolean shouldHttpClientRetry(int statusCode) {

        if (neverRetryCodes.contains(statusCode)) {
            return false;
        }

        // Retry all 500 errors
        if ((statusCode >= 500) && (statusCode <= 599)) {
            return true;
        }

        // Only status codes we will retry on
        return retryCodes.contains(statusCode);
    }

    @Override
    public boolean isSuccessStatusCode(int statusCode) {
        return successStatusCodes.contains(statusCode);
    }

    @Override
    public String httpRetryerSendOkMetric() {
        return PEP_HTTP_RETRYER_REQUEST_SEND_OK.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendFailMetric(Exception e) {
        return PEP_HTTP_RETRYER_REQUEST_SEND_FAIL.withEndpointPrefixAndExceptionName(e);
    }

    @Override
    public String httpRetryerSendErrorMetric() {
        return PEP_HTTP_RETRYER_SEND_ERROR.withEndpointPrefix();
    }

    @Override
    public String httpRetryerSendRetryMetric() {
        return PEP_HTTP_RETRYER_REQUEST_SEND_RETRY.withEndpointPrefix();
    }

    @Override
    public String httpRetryerMaxRetriesMetric() {
        return PEP_HTTP_RETRYER_SEND_MAX_RETRIES.withEndpointPrefix();
    }
}
