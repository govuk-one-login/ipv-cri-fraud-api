package uk.gov.di.ipv.cri.fraud.library.metrics;

import uk.gov.di.ipv.cri.fraud.library.exception.MetricException;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;

import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.API_RESPONSE_TYPE_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.API_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.API_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.API_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.API_RESPONSE_TYPE_VALID;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.HTTP_RETRYER_REQUEST_SEND_FAIL;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.HTTP_RETRYER_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.HTTP_RETRYER_REQUEST_SEND_RETRY;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.HTTP_RETRYER_SEND_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.HTTP_RETRYER_SEND_MAX_RETRIES;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.REQUEST_CREATED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetricType.REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIMetricEndpointPrefix.FRAUD_V1;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIMetricEndpointPrefix.PEP_V1;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIMetricEndpointPrefix.TOKEN;

public enum ThirdPartyAPIEndpointMetric {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Authenticate End Point Metrics
    ///////////////////////////////////////////////////////////////////////////////////////////////

    TOKEN_REQUEST_REUSING_CACHED_TOKEN(TOKEN, "reusing_cached_token"),
    TOKEN_REQUEST_CREATED(TOKEN, REQUEST_CREATED),
    TOKEN_REQUEST_SEND_OK(TOKEN, REQUEST_SEND_OK),
    TOKEN_REQUEST_SEND_ERROR(TOKEN, REQUEST_SEND_ERROR),
    TOKEN_RESPONSE_TYPE_VALID(TOKEN, API_RESPONSE_TYPE_VALID),
    TOKEN_RESPONSE_TYPE_INVALID(TOKEN, API_RESPONSE_TYPE_INVALID),
    TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS(TOKEN, API_RESPONSE_TYPE_EXPECTED_HTTP_STATUS),
    TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS(TOKEN, API_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS),
    TOKEN_RESPONSE_STATUS_CODE_ALERT_METRIC(TOKEN, "status_code_alert_metric"),
    TOKEN_HTTP_RETRYER_REQUEST_SEND_OK(TOKEN, HTTP_RETRYER_REQUEST_SEND_OK),
    TOKEN_HTTP_RETRYER_REQUEST_SEND_FAIL(TOKEN, HTTP_RETRYER_REQUEST_SEND_FAIL),
    TOKEN_HTTP_RETRYER_REQUEST_SEND_RETRY(TOKEN, HTTP_RETRYER_REQUEST_SEND_RETRY),
    TOKEN_HTTP_RETRYER_SEND_MAX_RETRIES(TOKEN, HTTP_RETRYER_SEND_MAX_RETRIES),
    TOKEN_HTTP_RETRYER_SEND_ERROR(TOKEN, HTTP_RETRYER_SEND_ERROR),
    TOKEN_RESPONSE_FAILED_TO_GENERATE_NEW_TOKEN_METRIC(TOKEN, "failed_to_generate_new_token"),

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // FRAUD End Point Metrics                                                                   //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    FRAUD_REQUEST_CREATED(FRAUD_V1, REQUEST_CREATED),
    FRAUD_REQUEST_SEND_OK(FRAUD_V1, REQUEST_SEND_OK),
    FRAUD_REQUEST_SEND_ERROR(FRAUD_V1, REQUEST_SEND_ERROR),

    FRAUD_RESPONSE_TYPE_VALID(FRAUD_V1, API_RESPONSE_TYPE_VALID),
    FRAUD_RESPONSE_TYPE_INVALID(FRAUD_V1, API_RESPONSE_TYPE_INVALID),

    FRAUD_RESPONSE_TYPE_ERROR(FRAUD_V1, API_RESPONSE_TYPE_ERROR),

    FRAUD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS(FRAUD_V1, API_RESPONSE_TYPE_EXPECTED_HTTP_STATUS),
    FRAUD_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS(FRAUD_V1, API_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS),

    FRAUD_HTTP_RETRYER_REQUEST_SEND_OK(FRAUD_V1, HTTP_RETRYER_REQUEST_SEND_OK),
    FRAUD_HTTP_RETRYER_REQUEST_SEND_FAIL(FRAUD_V1, HTTP_RETRYER_REQUEST_SEND_FAIL),
    FRAUD_HTTP_RETRYER_REQUEST_SEND_RETRY(FRAUD_V1, HTTP_RETRYER_REQUEST_SEND_RETRY),
    FRAUD_HTTP_RETRYER_SEND_MAX_RETRIES(FRAUD_V1, HTTP_RETRYER_SEND_MAX_RETRIES),
    FRAUD_HTTP_RETRYER_SEND_ERROR(FRAUD_V1, HTTP_RETRYER_SEND_ERROR),

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // PEP End Point Metrics                                                                   //
    ///////////////////////////////////////////////////////////////////////////////////////////////
    PEP_REQUEST_CREATED(PEP_V1, REQUEST_CREATED),
    PEP_REQUEST_SEND_OK(PEP_V1, REQUEST_SEND_OK),
    PEP_REQUEST_SEND_ERROR(PEP_V1, REQUEST_SEND_ERROR),

    PEP_RESPONSE_TYPE_VALID(PEP_V1, API_RESPONSE_TYPE_VALID),
    PEP_RESPONSE_TYPE_INVALID(PEP_V1, API_RESPONSE_TYPE_INVALID),

    PEP_RESPONSE_TYPE_ERROR(PEP_V1, API_RESPONSE_TYPE_ERROR),

    PEP_RESPONSE_TYPE_EXPECTED_HTTP_STATUS(PEP_V1, API_RESPONSE_TYPE_EXPECTED_HTTP_STATUS),
    PEP_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS(PEP_V1, API_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS),

    PEP_HTTP_RETRYER_REQUEST_SEND_OK(PEP_V1, HTTP_RETRYER_REQUEST_SEND_OK),
    PEP_HTTP_RETRYER_REQUEST_SEND_FAIL(PEP_V1, HTTP_RETRYER_REQUEST_SEND_FAIL),
    PEP_HTTP_RETRYER_REQUEST_SEND_RETRY(PEP_V1, HTTP_RETRYER_REQUEST_SEND_RETRY),
    PEP_HTTP_RETRYER_SEND_MAX_RETRIES(PEP_V1, HTTP_RETRYER_SEND_MAX_RETRIES),
    PEP_HTTP_RETRYER_SEND_ERROR(PEP_V1, HTTP_RETRYER_SEND_ERROR);

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // End Of Metric Descriptions                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static final String METRIC_FORMAT = "%s_%s";
    private static final String METRIC_CAUSE_FORMAT = METRIC_FORMAT;

    private final String metricWithEndpointPrefix;

    // To avoid copy and paste errors in the alternative large list of string mappings for each
    // endpoint metric combo
    ThirdPartyAPIEndpointMetric(
            ThirdPartyAPIMetricEndpointPrefix prefix, ThirdPartyAPIEndpointMetricType metricType) {
        String endPointPrefixLowerCase = prefix.toString().toLowerCase();
        String metricTypeLowercase = metricType.toString().toLowerCase();
        this.metricWithEndpointPrefix =
                String.format(METRIC_FORMAT, endPointPrefixLowerCase, metricTypeLowercase);
    }

    // To allow special case metrics that do not apply to all endpoints (eg UP/DOWN health)
    ThirdPartyAPIEndpointMetric(ThirdPartyAPIMetricEndpointPrefix prefix, String metric) {
        String endPointPrefixLowerCase = prefix.toString().toLowerCase();
        String metricLowercase = metric.toLowerCase();
        this.metricWithEndpointPrefix =
                String.format(METRIC_FORMAT, endPointPrefixLowerCase, metricLowercase);
    }

    public String withEndpointPrefix() {
        return metricWithEndpointPrefix;
    }

    /**
     * Created for attaching Exception to REQUEST_SEND_ERROR - format effectively - %s_%s_%s. NOTE:
     * invalid to provide OAuthErrorResponseException. OAuthErrorResponseException is a generated
     * exception, metrics should only capture caught executions.
     *
     * @return String in the format endpont_metric_exceptionname
     */
    public String withEndpointPrefixAndExceptionName(Exception e) {
        if (e instanceof OAuthErrorResponseException) {
            // OAuthErrorResponseException is a generated exception,
            // metrics should only capture caught executions
            throw new MetricException(
                    "OAuthErrorResponseException is not a valid exception for metrics");
        }

        return String.format(
                METRIC_CAUSE_FORMAT, metricWithEndpointPrefix, e.getClass().getSimpleName());
    }
}
