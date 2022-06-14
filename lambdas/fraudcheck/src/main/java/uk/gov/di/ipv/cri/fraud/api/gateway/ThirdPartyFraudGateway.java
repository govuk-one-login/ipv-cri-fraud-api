package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.IdentityVerificationRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.util.SleepHelper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class ThirdPartyFraudGateway {

    private static final Logger LOGGER = LogManager.getLogger();

    private final HttpClient httpClient;
    private final IdentityVerificationRequestMapper requestMapper;
    private final IdentityVerificationResponseMapper responseMapper;
    private final ObjectMapper objectMapper;
    private final HmacGenerator hmacGenerator;
    private final URI endpointUri;
    private final SleepHelper sleepHelper;

    public static final String HTTP_300_REDIRECT_MESSAGE =
            "Redirection Message returned from Fraud Check Response, Status Code - ";
    public static final String HTTP_400_CLIENT_REQUEST_ERROR =
            "Client Request Error returned from Fraud Check Response, Status Code - ";
    public static final String HTTP_500_SERVER_ERROR =
            "Server Error returned from Fraud Check Response, Status Code - ";

    public static final String HTTP_UNHANDLED_ERROR =
            "Unhandled HTTP Response from Fraud Check Response, Status Code - ";
    public static final int MAX_HTTP_RETRIES = 7;
    public static final long HTTP_RETRY_WAIT_TIME_LIMIT_MS = 12800L;

    public ThirdPartyFraudGateway(
            HttpClient httpClient,
            IdentityVerificationRequestMapper requestMapper,
            IdentityVerificationResponseMapper responseMapper,
            ObjectMapper objectMapper,
            HmacGenerator hmacGenerator,
            String endpointUrl) {
        Objects.requireNonNull(httpClient, "httpClient must not be null");
        Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        Objects.requireNonNull(responseMapper, "responseMapper must not be null");
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(hmacGenerator, "hmacGenerator must not be null");
        Objects.requireNonNull(endpointUrl, "endpointUri must not be null");
        if (StringUtils.isBlank(endpointUrl)) {
            throw new IllegalArgumentException("endpointUrl must be specified");
        }
        this.httpClient = httpClient;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
        this.objectMapper = objectMapper;
        this.hmacGenerator = hmacGenerator;
        this.endpointUri = URI.create(endpointUrl);
        this.sleepHelper = new SleepHelper(HTTP_RETRY_WAIT_TIME_LIMIT_MS);
    }

    public ThirdPartyFraudGateway(
            HttpClient httpClient,
            IdentityVerificationRequestMapper requestMapper,
            IdentityVerificationResponseMapper responseMapper,
            ObjectMapper objectMapper,
            HmacGenerator hmacGenerator,
            String endpointUrl,
            SleepHelper sleepHelper) {
        Objects.requireNonNull(httpClient, "httpClient must not be null");
        Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        Objects.requireNonNull(responseMapper, "responseMapper must not be null");
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(hmacGenerator, "hmacGenerator must not be null");
        Objects.requireNonNull(endpointUrl, "endpointUri must not be null");
        if (StringUtils.isBlank(endpointUrl)) {
            throw new IllegalArgumentException("endpointUrl must be specified");
        }
        Objects.requireNonNull(sleepHelper, "sleepHelper must not be null");
        this.httpClient = httpClient;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
        this.objectMapper = objectMapper;
        this.hmacGenerator = hmacGenerator;
        this.endpointUri = URI.create(endpointUrl);
        this.sleepHelper = sleepHelper;
    }

    public FraudCheckResult performFraudCheck(IdentityVerificationRequest personIdentity)
            throws IOException, InterruptedException {
        LOGGER.info("Mapping person to third party verification request");

        // IdentityVerificationRequest apiRequest = requestMapper.mapPersonIdentity(personIdentity);
        String requestBody = objectMapper.writeValueAsString(personIdentity);
        LOGGER.info("Person identity request {}", requestBody);

        String requestBodyHmac = hmacGenerator.generateHmac(requestBody);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(endpointUri)
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("hmac-signature", requestBodyHmac)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        LOGGER.info("Submitting fraud check request to third party...");
        HttpResponse<String> httpResponse = sendHTTPRequestRetryIfAllowed(request);

        int statusCode = httpResponse.statusCode();
        LOGGER.info("Third party response code {}", statusCode);

        if (statusCode == 200) {
            String responseBody = httpResponse.body();
            IdentityVerificationResponse response =
                    objectMapper.readValue(responseBody, IdentityVerificationResponse.class);
            LOGGER.info("Thirdparty response: {}", objectMapper.writeValueAsString(response));
            return responseMapper.mapIdentityVerificationResponse(response);
        } else {
            FraudCheckResult fraudCheckResult = new FraudCheckResult();
            fraudCheckResult.setExecutedSuccessfully(false);

            if (statusCode >= 300 && statusCode <= 399) {
                fraudCheckResult.setErrorMessage(HTTP_300_REDIRECT_MESSAGE + statusCode);
            } else if (statusCode >= 400 && statusCode <= 499) {
                fraudCheckResult.setErrorMessage(HTTP_400_CLIENT_REQUEST_ERROR + statusCode);
            } else if (statusCode >= 500 && statusCode <= 599) {
                fraudCheckResult.setErrorMessage(HTTP_500_SERVER_ERROR + statusCode);
            } else {
                fraudCheckResult.setErrorMessage(HTTP_UNHANDLED_ERROR + statusCode);
            }

            return fraudCheckResult;
        }
    }

    private HttpResponse<String> sendHTTPRequestRetryIfAllowed(HttpRequest request)
            throws InterruptedException, IOException {

        HttpResponse<String> httpResponse = null;

        // 0 is initial request, > 0 are retries
        int tryCount = 0;
        boolean retry = false;

        do {
            // Wait before sending request (0ms for first try)
            sleepHelper.sleepWithExponentialBackOff(tryCount);

            try {
                httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                retry = shouldHttpClientRetry(httpResponse.statusCode());

                LOGGER.info(
                        "HTTPRequestRetry - totalRequests {}, retries {}, retryNeeded {}, statusCode {}",
                        tryCount + 1,
                        tryCount,
                        retry,
                        httpResponse.statusCode());

            } catch (IOException e) {
                if (!(e instanceof HttpConnectTimeoutException)) {
                    throw e;
                }

                // For retries (tryCount>0) we want to rethrow only the last
                // HttpConnectTimeoutException
                if (tryCount < MAX_HTTP_RETRIES) {

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

                    throw e;
                }
            }
        } while (retry && (tryCount++ < MAX_HTTP_RETRIES));

        LOGGER.info("HTTPRequestRetry Exited lastStatusCode {}", httpResponse.statusCode());

        return httpResponse;
    }

    private boolean shouldHttpClientRetry(int statusCode) {
        if (statusCode == 200) {
            // OK, Success
            return false;
        } else if (statusCode == 429) {
            // Too many recent requests
            LOGGER.warn("shouldHttpClientRetry statusCode - {}", statusCode);
            return true;
        } else {
            // Retry all server errors, but not any other status codes
            return ((statusCode >= 500) && (statusCode <= 599));
        }
    }
}
