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
import uk.gov.di.ipv.cri.fraud.api.domain.check.PepCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.PEPRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.PEPResponse;
import uk.gov.di.ipv.cri.fraud.api.service.FraudCheckConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.HttpRetryStatusConfig;
import uk.gov.di.ipv.cri.fraud.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.fraud.api.service.PepCheckHttpRetryStatusConfig;
import uk.gov.di.ipv.cri.fraud.api.util.HTTPReply;
import uk.gov.di.ipv.cri.fraud.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse.ERROR_PEP_CHECK_RETURNED_UNEXPECTED_HTTP_STATUS_CODE;
import static uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse.FAILED_TO_CREATE_API_REQUEST_FOR_PEP_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_LATENCY_MILLIS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_RESPONSE_TYPE_VALID;

public class ThirdPartyPepGateway {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String REQUEST_NAME = "Pep Check";

    private static final String APPLICATION_JSON_HEADER = "application/json";

    private final FraudCheckConfigurationService fraudCheckConfigurationService;
    private final IdentityVerificationRequestMapper requestMapper;
    private final IdentityVerificationResponseMapper responseMapper;
    private final ObjectMapper objectMapper;
    private final HmacGenerator hmacGenerator;

    private final URI endpointUri;

    private final EventProbe eventProbe;
    private final Clock clock;

    // HTTP
    private final HttpRetryStatusConfig pepHttpRetryStatusConfig;
    private final HttpRetryer httpRetryer;

    // POOL_REQ + CONN_EST + HTTP_RESP = overall max timeout
    private static final int POOL_REQUEST_TIMEOUT_MS = 5000;
    private static final int CONNECTION_ESTABLISHMENT_TIMEOUT_MS = 5000;
    public static final int PEP_HTTP_RESPONSE_TIMEOUT_MS = 20000;
    private final RequestConfig pepCheckRequestConfig;

    public ThirdPartyPepGateway(
            HttpRetryer httpRetryer,
            IdentityVerificationRequestMapper requestMapper,
            IdentityVerificationResponseMapper responseMapper,
            ObjectMapper objectMapper,
            HmacGenerator hmacGenerator,
            String endpointUrl,
            FraudCheckConfigurationService fraudCheckConfigurationService,
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
        this.fraudCheckConfigurationService = fraudCheckConfigurationService;
        this.eventProbe = eventProbe;
        this.clock = Clock.systemUTC();

        this.pepHttpRetryStatusConfig = new PepCheckHttpRetryStatusConfig();

        this.pepCheckRequestConfig =
                HttpRequestConfig.getCustomRequestConfig(
                        POOL_REQUEST_TIMEOUT_MS,
                        CONNECTION_ESTABLISHMENT_TIMEOUT_MS,
                        PEP_HTTP_RESPONSE_TIMEOUT_MS);
    }

    public PepCheckResult performPepCheck(
            PersonIdentity personIdentity, boolean crosscoreV2Enabled, String token)
            throws OAuthErrorResponseException {
        LOGGER.info("Mapping person to {} request", REQUEST_NAME);

        String tenantId = fraudCheckConfigurationService.getTenantId();
        if (crosscoreV2Enabled) {
            tenantId = fraudCheckConfigurationService.getCrosscoreV2Configuration().getTenantId();
        }

        PEPRequest apiRequest = requestMapper.mapPEPPersonIdentity(personIdentity, tenantId);

        String requestBody = null;
        try {
            requestBody = objectMapper.writeValueAsString(apiRequest);
        } catch (JsonProcessingException e) {

            // PII in variables
            LOGGER.error(
                    "JsonProcessingException {}",
                    FAILED_TO_CREATE_API_REQUEST_FOR_PEP_CHECK.getMessage());
            LOGGER.debug(e.getMessage());

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    FAILED_TO_CREATE_API_REQUEST_FOR_PEP_CHECK);
        }

        LOGGER.debug("{} Request {}", REQUEST_NAME, requestBody);
        HttpPost postRequest = httpRequestBuilder(requestBody, crosscoreV2Enabled, token);

        // Enforce connection timeout values
        postRequest.setConfig(pepCheckRequestConfig);

        eventProbe.counterMetric(PEP_REQUEST_CREATED.withEndpointPrefix());

        Instant startCheck = clock.instant();

        final HTTPReply httpReply;
        LOGGER.info("Submitting {} request...", REQUEST_NAME);
        try {
            httpReply =
                    httpRetryer.sendHTTPRequestRetryIfAllowed(
                            postRequest, pepHttpRetryStatusConfig, REQUEST_NAME);
            eventProbe.counterMetric(PEP_REQUEST_SEND_OK.withEndpointPrefix());
            // throws OAuthErrorResponseException on error
        } catch (IOException e) {
            LOGGER.error("IOException executing {} http request {}", REQUEST_NAME, e.getMessage());
            eventProbe.counterMetric(PEP_REQUEST_SEND_ERROR.withEndpointPrefix());

            captureAndLogRequestLatency(startCheck);

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_SENDING_PEP_CHECK_REQUEST);
        }

        captureAndLogRequestLatency(startCheck);

        return pepCheckResponseHandler(httpReply);
    }

    private void captureAndLogRequestLatency(Instant startCheck) {
        long latency = Duration.between(startCheck, clock.instant()).toMillis();
        eventProbe.counterMetric(THIRD_PARTY_PEP_RESPONSE_LATENCY_MILLIS, latency);
        LOGGER.info("{} latency {}", REQUEST_NAME, latency);
    }

    private HttpPost httpRequestBuilder(
            String requestBody, boolean crosscoreV2Enabled, String token) {
        HttpPost request;
        if (crosscoreV2Enabled) {
            request =
                    new HttpPost(
                            fraudCheckConfigurationService
                                    .getCrosscoreV2Configuration()
                                    .getEndpointUri());
            request.addHeader("Content-Type", APPLICATION_JSON_HEADER);
            request.addHeader("Accept", APPLICATION_JSON_HEADER);
            request.addHeader("Authorization", "Bearer " + token);
            request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
        } else { // EMTODO: Remove conditional in post v2 cleanup
            String requestBodyHmacSignature = hmacGenerator.generateHmac(requestBody);

            request = new HttpPost(endpointUri);
            request.addHeader("Content-Type", APPLICATION_JSON_HEADER);
            request.addHeader("hmac-signature", requestBodyHmacSignature);
            request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
        }
        return request;
    }

    private PepCheckResult pepCheckResponseHandler(HTTPReply httpReply)
            throws OAuthErrorResponseException {

        if (null != httpReply && httpReply.getStatusCode() == 200) {
            LOGGER.info("{} response code {}", REQUEST_NAME, httpReply.getStatusCode());

            eventProbe.counterMetric(PEP_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());

            String responseBody = httpReply.responseBody;
            LOGGER.debug("{} response {}", REQUEST_NAME, responseBody);
            PEPResponse pepResponse;

            try {
                pepResponse = objectMapper.readValue(responseBody, PEPResponse.class);
            } catch (JsonProcessingException e) {
                LOGGER.error("JsonProcessingException mapping {} response", REQUEST_NAME);
                LOGGER.debug(e.getMessage());

                eventProbe.counterMetric(PEP_RESPONSE_TYPE_INVALID.withEndpointPrefix());

                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_MAP_PEP_CHECK_RESPONSE_BODY);
            }

            eventProbe.counterMetric(PEP_RESPONSE_TYPE_VALID.withEndpointPrefix());

            return responseMapper.mapPEPResponse(pepResponse);
        } else {
            if (null != httpReply) {
                LOGGER.info("{} response code {}", REQUEST_NAME, httpReply.getStatusCode());
            }
            eventProbe.counterMetric(PEP_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());

            PepCheckResult pepCheckResult = new PepCheckResult();
            pepCheckResult.setExecutedSuccessfully(false);

            pepCheckResult.setErrorMessage(
                    ERROR_PEP_CHECK_RETURNED_UNEXPECTED_HTTP_STATUS_CODE.getMessage());

            return pepCheckResult;
        }
    }
}
