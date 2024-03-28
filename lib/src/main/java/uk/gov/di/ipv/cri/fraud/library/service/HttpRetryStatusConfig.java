package uk.gov.di.ipv.cri.fraud.library.service;

public interface HttpRetryStatusConfig {
    // Call back to allow configuring the status codes to retry per api/endpoint
    boolean shouldHttpClientRetry(int statusCode);

    // Typically 200, is the only success code,
    // API's may have others depending on implementation
    boolean isSuccessStatusCode(int statusCode);

    // Call backs to allow http retry metrics to be captured per api/endpoint
    String httpRetryerSendOkMetric();

    String httpRetryerSendFailMetric(Exception e);

    String httpRetryerSendErrorMetric();

    String httpRetryerSendRetryMetric();

    String httpRetryerMaxRetriesMetric();
}
