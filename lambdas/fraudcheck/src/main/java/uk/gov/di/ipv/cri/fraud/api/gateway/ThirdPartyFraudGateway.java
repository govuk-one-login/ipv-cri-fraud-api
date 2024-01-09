package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.check.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.IdentityVerificationRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.service.FraudCheckHttpRetryStatusConfig;
import uk.gov.di.ipv.cri.fraud.api.service.HttpRetryStatusConfig;
import uk.gov.di.ipv.cri.fraud.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.fraud.api.util.HTTPReply;
import uk.gov.di.ipv.cri.fraud.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

import static uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse.ERROR_FRAUD_CHECK_RETURNED_UNEXPECTED_HTTP_STATUS_CODE;
import static uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse.ERROR_SENDING_FRAUD_CHECK_REQUEST;
import static uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse.FAILED_TO_CREATE_API_REQUEST_FOR_FRAUD_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_FRAUD_RESPONSE_LATENCY_MILLIS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_VALID;

public class ThirdPartyFraudGateway {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String REQUEST_NAME = "Fraud Check";

    private final IdentityVerificationRequestMapper requestMapper;
    private final IdentityVerificationResponseMapper responseMapper;
    private final ObjectMapper objectMapper;
    private final HmacGenerator hmacGenerator;

    private final URI endpointUri;

    private final EventProbe eventProbe;
    private final Clock clock;

    // HTTP
    private final HttpRetryStatusConfig fraudHttpRetryStatusConfig;
    private final HttpRetryer httpRetryer;

    // POOL_REQ + CONN_EST + HTTP_RESP = overall max timeout
    private static final int POOL_REQUEST_TIMEOUT_MS = 5000;
    private static final int CONNECTION_ESTABLISHMENT_TIMEOUT_MS = 5000;
    public static final int FRAUD_HTTP_RESPONSE_TIMEOUT_MS = 5000;
    private final RequestConfig fraudCheckRequestConfig;

    public ThirdPartyFraudGateway(
            HttpRetryer httpRetryer,
            IdentityVerificationRequestMapper requestMapper,
            IdentityVerificationResponseMapper responseMapper,
            ObjectMapper objectMapper,
            HmacGenerator hmacGenerator,
            String endpointUrl,
            EventProbe eventProbe) {
        Objects.requireNonNull(httpRetryer, "httpClient must not be null");
        Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        Objects.requireNonNull(responseMapper, "responseMapper must not be null");
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(hmacGenerator, "hmacGenerator must not be null");
        Objects.requireNonNull(endpointUrl, "endpointUri must not be null");
        if (StringUtils.isBlank(endpointUrl)) {
            throw new IllegalArgumentException("endpointUrl must be specified");
        }
        this.httpRetryer = httpRetryer;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
        this.objectMapper = objectMapper;
        this.hmacGenerator = hmacGenerator;
        this.endpointUri = URI.create(endpointUrl);
        this.eventProbe = eventProbe;
        this.clock = Clock.systemUTC();

        this.fraudHttpRetryStatusConfig = new FraudCheckHttpRetryStatusConfig();

        this.fraudCheckRequestConfig =
                HttpRequestConfig.getCustomRequestConfig(
                        POOL_REQUEST_TIMEOUT_MS,
                        CONNECTION_ESTABLISHMENT_TIMEOUT_MS,
                        FRAUD_HTTP_RESPONSE_TIMEOUT_MS);
    }

    private HttpPost httpRequestBuilder(String requestBody) {
        String requestBodyHmacSignature = hmacGenerator.generateHmac(requestBody);

        HttpPost request = new HttpPost(endpointUri);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("hmac-signature", requestBodyHmacSignature);
        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
        return request;
    }

    public FraudCheckResult performFraudCheck(PersonIdentity personIdentity)
            throws OAuthErrorResponseException {
        LOGGER.info("Mapping person to {} request", REQUEST_NAME);
        IdentityVerificationRequest apiRequest = requestMapper.mapPersonIdentity(personIdentity);

        String requestBody = null;
        try {
            requestBody = objectMapper.writeValueAsString(apiRequest);
        } catch (JsonProcessingException e) {

            // PII in variables
            LOGGER.error(
                    "JsonProcessingException {}",
                    FAILED_TO_CREATE_API_REQUEST_FOR_FRAUD_CHECK.getMessage());
            LOGGER.debug(e.getMessage());

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_CREATE_API_REQUEST_FOR_FRAUD_CHECK);
        }

        LOGGER.debug("{} Request {}", REQUEST_NAME, requestBody);
        HttpPost postRequest = httpRequestBuilder(requestBody);

        // Enforce connection timeout values
        postRequest.setConfig(fraudCheckRequestConfig);

        eventProbe.counterMetric(FRAUD_REQUEST_CREATED.withEndpointPrefix());

        var startCheck = clock.instant();

        final HTTPReply httpReply;
        LOGGER.info("Submitting {} request", REQUEST_NAME);
        try {
            httpReply =
                    httpRetryer.sendHTTPRequestRetryIfAllowed(
                            postRequest, fraudHttpRetryStatusConfig, REQUEST_NAME);
            eventProbe.counterMetric(FRAUD_REQUEST_SEND_OK.withEndpointPrefix());
            // throws OAuthErrorResponseException on error
        } catch (IOException e) {
            LOGGER.error("IOException executing {} http request {}", REQUEST_NAME, e.getMessage());
            eventProbe.counterMetric(FRAUD_REQUEST_SEND_ERROR.withEndpointPrefix());
            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ERROR_SENDING_FRAUD_CHECK_REQUEST);
        }

        long latency = Duration.between(startCheck, clock.instant()).toMillis();
        eventProbe.counterMetric(THIRD_PARTY_FRAUD_RESPONSE_LATENCY_MILLIS, latency);
        LOGGER.info("{} latency {}", REQUEST_NAME, latency);

        return fraudCheckResponseHandler(httpReply);
    }

    private FraudCheckResult fraudCheckResponseHandler(HTTPReply httpReply)
            throws OAuthErrorResponseException {

        if (null != httpReply && httpReply.getStatusCode() == 200) {
            LOGGER.info("{} response code {}", REQUEST_NAME, httpReply.getStatusCode());

            eventProbe.counterMetric(FRAUD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());

            String responseBody = httpReply.responseBody;
            LOGGER.debug("{} response {}", REQUEST_NAME, responseBody);
            IdentityVerificationResponse fraudCheckResponse;

            try {
                fraudCheckResponse =
                        objectMapper.readValue(responseBody, IdentityVerificationResponse.class);
            } catch (JsonProcessingException e) {
                LOGGER.error("JsonProcessingException mapping {} response", REQUEST_NAME);
                LOGGER.debug(e.getMessage());

                eventProbe.counterMetric(FRAUD_RESPONSE_TYPE_INVALID.withEndpointPrefix());

                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_MAP_FRAUD_CHECK_RESPONSE_BODY);
            }

            // Note this refers to the API response being able to be object mapped correctly
            eventProbe.counterMetric(FRAUD_RESPONSE_TYPE_VALID.withEndpointPrefix());

            return responseMapper.mapFraudResponse(fraudCheckResponse);

        } else {
            if (null != httpReply) {
                LOGGER.info("{} response code {}", REQUEST_NAME, httpReply.getStatusCode());
            }

            eventProbe.counterMetric(
                    FRAUD_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());

            FraudCheckResult fraudCheckResult = new FraudCheckResult();
            fraudCheckResult.setExecutedSuccessfully(false);

            fraudCheckResult.setErrorMessage(
                    ERROR_FRAUD_CHECK_RETURNED_UNEXPECTED_HTTP_STATUS_CODE.getMessage());

            return fraudCheckResult;
        }
    }
}
