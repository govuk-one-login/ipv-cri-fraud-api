package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.di.ipv.cri.fraud.api.service.FraudCheckConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.FraudCheckHttpRetryStatusConfig;
import uk.gov.di.ipv.cri.fraud.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.fraud.library.service.HttpRetryStatusConfig;
import uk.gov.di.ipv.cri.fraud.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.fraud.library.strategy.Strategy;
import uk.gov.di.ipv.cri.fraud.library.util.HTTPReply;
import uk.gov.di.ipv.cri.fraud.library.util.StopWatch;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import static uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse.ERROR_FRAUD_CHECK_RETURNED_UNEXPECTED_HTTP_STATUS_CODE;
import static uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse.ERROR_SENDING_FRAUD_CHECK_REQUEST;
import static uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse.FAILED_TO_CREATE_API_REQUEST_FOR_FRAUD_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_VALID;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.THIRD_PARTY_FRAUD_RESPONSE_LATENCY;

public class ThirdPartyFraudGateway {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String REQUEST_NAME = "Fraud Check";

    private static final String APPLICATION_JSON_HEADER = "application/json";

    private final FraudCheckConfigurationService fraudCheckConfigurationService;
    private final IdentityVerificationRequestMapper requestMapper;
    private final IdentityVerificationResponseMapper responseMapper;
    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;

    // HTTP
    private final HttpRetryStatusConfig fraudHttpRetryStatusConfig;
    private final HttpRetryer httpRetryer;

    // POOL_REQ + CONN_EST + HTTP_RESP = overall max timeout
    private static final int POOL_REQUEST_TIMEOUT_MS = 5000;
    private static final int CONNECTION_ESTABLISHMENT_TIMEOUT_MS = 5000;
    public static final int FRAUD_HTTP_RESPONSE_TIMEOUT_MS = 5000;
    private final RequestConfig fraudCheckRequestConfig;

    private final StopWatch stopWatch;

    public ThirdPartyFraudGateway(
            HttpRetryer httpRetryer,
            IdentityVerificationRequestMapper requestMapper,
            IdentityVerificationResponseMapper responseMapper,
            ObjectMapper objectMapper,
            FraudCheckConfigurationService fraudCheckConfigurationService,
            EventProbe eventProbe) {
        Objects.requireNonNull(httpRetryer, "httpClient must not be null");
        Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        Objects.requireNonNull(responseMapper, "responseMapper must not be null");
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(
                fraudCheckConfigurationService, "fraudCheckConfigurationService must not be null");
        this.httpRetryer = httpRetryer;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
        this.objectMapper = objectMapper;
        this.fraudCheckConfigurationService = fraudCheckConfigurationService;
        this.eventProbe = eventProbe;

        this.fraudHttpRetryStatusConfig = new FraudCheckHttpRetryStatusConfig();

        this.fraudCheckRequestConfig =
                HttpRequestConfig.getCustomRequestConfig(
                        POOL_REQUEST_TIMEOUT_MS,
                        CONNECTION_ESTABLISHMENT_TIMEOUT_MS,
                        FRAUD_HTTP_RESPONSE_TIMEOUT_MS);

        this.stopWatch = new StopWatch();
    }

    public FraudCheckResult performFraudCheck(
            PersonIdentity personIdentity, String token, Strategy strategy)
            throws OAuthErrorResponseException {
        LOGGER.info("Mapping person to {} request", REQUEST_NAME);
        String tenantId =
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getTenantId();

        IdentityVerificationRequest apiRequest =
                requestMapper.mapPersonIdentity(personIdentity, tenantId);

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
        HttpPost postRequest = httpRequestBuilder(requestBody, token, strategy);

        // Enforce connection timeout values
        postRequest.setConfig(fraudCheckRequestConfig);

        eventProbe.counterMetric(FRAUD_REQUEST_CREATED.withEndpointPrefix());

        final HTTPReply httpReply;
        LOGGER.info(
                "Submitting {} request... ClientReferenceId {}",
                REQUEST_NAME,
                apiRequest.getHeader().getClientReferenceId());
        stopWatch.start();
        try {
            httpReply =
                    httpRetryer.sendHTTPRequestRetryIfAllowed(
                            postRequest, fraudHttpRetryStatusConfig, REQUEST_NAME);
            eventProbe.counterMetric(FRAUD_REQUEST_SEND_OK.withEndpointPrefix());
            // throws OAuthErrorResponseException on error
        } catch (IOException e) {
            LOGGER.error("IOException executing {} http request {}", REQUEST_NAME, e.getMessage());
            eventProbe.counterMetric(FRAUD_REQUEST_SEND_ERROR.withEndpointPrefix());

            captureAndLogRequestLatency();

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ERROR_SENDING_FRAUD_CHECK_REQUEST);
        }

        captureAndLogRequestLatency();

        return fraudCheckResponseHandler(httpReply);
    }

    private void captureAndLogRequestLatency() {
        long latency = stopWatch.stop();
        eventProbe.counterMetric(
                THIRD_PARTY_FRAUD_RESPONSE_LATENCY.toString().toLowerCase(), stopWatch.stop());
        LOGGER.info("{} latency {}", REQUEST_NAME, latency);
    }

    private HttpPost httpRequestBuilder(String requestBody, String token, Strategy strategy) {
        HttpPost request = new HttpPost(selectRequestURI(strategy));
        request.addHeader("Content-Type", APPLICATION_JSON_HEADER);
        request.addHeader("Accept", APPLICATION_JSON_HEADER);
        request.addHeader("Authorization", "Bearer " + token);
        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
        return request;
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

    private URI selectRequestURI(Strategy strategy) {
        if (strategy == Strategy.NO_CHANGE) {
            return URI.create(
                    fraudCheckConfigurationService.getCrosscoreV2Configuration().getEndpointUri());
        } else {
            return URI.create(
                    fraudCheckConfigurationService
                            .getCrosscoreV2Configuration()
                            .getEndpointURIs()
                            .get(strategy.name()));
        }
    }
}
