package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.RequestHeaderKeys;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.TestStrategyClientId;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.TokenRequestPayload;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.TokenResponse;
import uk.gov.di.ipv.cri.fraud.api.persistence.item.TokenItem;
import uk.gov.di.ipv.cri.fraud.api.util.AccessTokenValidator;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.fraud.library.exception.TokenExpiryWindowException;
import uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric;
import uk.gov.di.ipv.cri.fraud.library.service.HttpRetryStatusConfig;
import uk.gov.di.ipv.cri.fraud.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.fraud.library.util.HTTPReply;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TokenRequestService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String ENDPOINT_NAME = "token endpoint";
    private static final String REQUEST_NAME = "Token";

    private CrosscoreV2Configuration crosscoreV2Configuration;
    private final String tokenTableName;
    private DataStore<TokenItem> dataStore;

    private final String clientSecret;
    private final String clientId;
    private final String username;
    private final String password;
    private final String userDomain;

    private final HttpRetryer httpRetryer;
    private final RequestConfig requestConfig;

    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;
    private final HttpRetryStatusConfig httpRetryStatusConfig;

    // Alerts will be fired for these status code responses
    // The CRI must also never retry request with these codes
    private final List<Integer> alertStatusCodes = List.of(400, 401);

    // Token item shared between concurrent lambdas (if scaling)
    public static final String TOKEN_ITEM_ID = "TokenKey";

    // DynamoDB auto ttl deletion is the best effort (upto 48hrs later...)
    // Token Item ttl expiration enforced CRI side (vs dynamo filter expression)
    // as there will be only ever be one token.
    private static final long MAX_ALLOWED_TOKEN_LIFETIME_SECONDS = 1800L;
    private static final long TOKEN_EXPIRATION_WINDOW_SECONDS = 300L;
    private static final long TOKEN_ITEM_TTL_SECS =
            MAX_ALLOWED_TOKEN_LIFETIME_SECONDS - TOKEN_EXPIRATION_WINDOW_SECONDS;

    public static final String INVALID_EXPIRY_WINDOW_ERROR_MESSAGE =
            "Token expiry window not valid";

    public TokenRequestService(
            CrosscoreV2Configuration crosscoreV2Configuration,
            DynamoDbEnhancedClient dynamoDbEnhancedClient,
            HttpRetryer httpRetryer,
            RequestConfig requestConfig,
            ObjectMapper objectMapper,
            EventProbe eventProbe) {

        this.crosscoreV2Configuration = crosscoreV2Configuration;

        // Token Table
        this.tokenTableName = crosscoreV2Configuration.getTokenTableName();
        this.dataStore = new DataStore<>(tokenTableName, TokenItem.class, dynamoDbEnhancedClient);

        this.clientSecret = crosscoreV2Configuration.getClientSecret();
        this.clientId = crosscoreV2Configuration.getClientId();
        this.username = crosscoreV2Configuration.getUsername();
        this.password = crosscoreV2Configuration.getPassword();
        this.userDomain = crosscoreV2Configuration.getUserDomain();

        this.httpRetryer = httpRetryer;
        this.requestConfig = requestConfig;

        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;

        this.httpRetryStatusConfig = new TokenHttpRetryStatusConfig();
    }

    public String requestToken(
            boolean alwaysRequestNewToken, TestStrategyClientId thirdPartyRouting)
            throws OAuthErrorResponseException {
        LOGGER.info("Checking Table {} for existing cached token", tokenTableName);

        TokenItem tokenItem = getTokenItemFromTable();

        boolean existingCachedToken = tokenItem != null;
        boolean tokenTtlHasExpired =
                existingCachedToken
                        && isTokenNearExpiration(tokenItem, TOKEN_EXPIRATION_WINDOW_SECONDS);

        LOGGER.info(
                "Existing cached token - {} - ttl expired {}",
                existingCachedToken,
                tokenTtlHasExpired);

        if (alwaysRequestNewToken) {
            LOGGER.info("Override enabled - requesting a new token");
        }

        boolean newTokenRequest =
                alwaysRequestNewToken || !existingCachedToken || tokenTtlHasExpired;

        // Request an Access Token
        if (newTokenRequest) {
            try {
                TokenResponse newTokenResponse = performNewTokenRequest(thirdPartyRouting);
                LOGGER.debug("Saving Token {}", newTokenResponse.getAccessToken());

                tokenItem = new TokenItem(newTokenResponse.getAccessToken());

                saveTokenItem(tokenItem);
            } catch (Exception exception) {
                LOGGER.error(
                        "Failed to generate new token. Continuing to use existing token ",
                        exception);
                // Alarm Firing
                eventProbe.counterMetric(
                        ThirdPartyAPIEndpointMetric
                                .TOKEN_RESPONSE_FAILED_TO_GENERATE_NEW_TOKEN_METRIC
                                .withEndpointPrefix());
                if (tokenItem == null) {
                    LOGGER.error(
                            "Failed to generate new token. No valid tokens present returning error ",
                            exception);
                    throw exception;
                }
            }
        } else {
            long ttl = tokenItem.getTtl();

            LOGGER.info(
                    "Re-using cached Token - expires {} UTC",
                    Instant.ofEpochSecond(ttl).atZone(ZoneId.systemDefault()).toLocalDateTime());

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.TOKEN_REQUEST_REUSING_CACHED_TOKEN
                            .withEndpointPrefix());
        }

        return tokenItem.getTokenValue();
    }

    private TokenResponse performNewTokenRequest(TestStrategyClientId thirdPartyRouting)
            throws OAuthErrorResponseException {
        URI requestURI = selectRequestURI(thirdPartyRouting);

        final String correlationId = UUID.randomUUID().toString();
        LOGGER.info("{} Correlation Id {}", REQUEST_NAME, correlationId);

        // Token Request is posted as if via a form
        final HttpPost request = new HttpPost();
        request.setURI(requestURI);
        request.addHeader(
                RequestHeaderKeys.HEADER_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.addHeader(RequestHeaderKeys.HEADER_CORRELATION_ID, correlationId);
        request.addHeader(RequestHeaderKeys.HEADER_USER_DOMAIN, userDomain);

        // Enforce connection timeout values
        request.setConfig(requestConfig);

        String requestBody;
        try {
            TokenRequestPayload tokenRequestPayload =
                    TokenRequestPayload.builder()
                            .userName(username)
                            .password(password)
                            .clientId(clientId)
                            .clientSecret(clientSecret)
                            .build();

            requestBody = objectMapper.writeValueAsString(tokenRequestPayload);
        } catch (JsonProcessingException e) {
            LOGGER.error("JsonProcessingException creating request body");
            LOGGER.debug(e.getMessage());

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.TOKEN_RESPONSE_TYPE_INVALID.withEndpointPrefix());

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_PREPARE_TOKEN_REQUEST_PAYLOAD);
        }

        LOGGER.debug(
                "{} request headers : {}",
                ENDPOINT_NAME,
                LOGGER.isDebugEnabled() ? (Arrays.toString(request.getAllHeaders())) : "");
        LOGGER.debug("{} request body : {}", REQUEST_NAME, requestBody);

        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        eventProbe.counterMetric(
                ThirdPartyAPIEndpointMetric.TOKEN_REQUEST_CREATED.withEndpointPrefix());

        final HTTPReply httpReply;
        String requestURIString = requestURI.toString();
        LOGGER.debug("{} request endpoint is {}", REQUEST_NAME, requestURIString);
        LOGGER.info("Submitting {} request to third party...", REQUEST_NAME);
        try {
            httpReply =
                    httpRetryer.sendHTTPRequestRetryIfAllowed(
                            request, httpRetryStatusConfig, REQUEST_NAME);
            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        } catch (IOException e) {

            LOGGER.error("IOException executing {} request - {}", REQUEST_NAME, e.getMessage());

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.TOKEN_REQUEST_SEND_ERROR
                            .withEndpointPrefixAndExceptionName(e));

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_TOKEN_ENDPOINT);
        }

        if (httpReply.statusCode == 200) {
            LOGGER.info("{} status code {}", REQUEST_NAME, httpReply.statusCode);

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS
                            .withEndpointPrefix());

            try {
                LOGGER.debug("{} headers {}", REQUEST_NAME, httpReply.responseHeaders);
                LOGGER.debug("{} response {}", REQUEST_NAME, httpReply.responseBody);

                TokenResponse response =
                        objectMapper.readValue(httpReply.responseBody, TokenResponse.class);

                // Validate token JWT
                boolean isValidToken =
                        AccessTokenValidator.isTokenValid(
                                response.getAccessToken(), objectMapper, crosscoreV2Configuration);

                if (!isValidToken) {
                    eventProbe.counterMetric(
                            ThirdPartyAPIEndpointMetric.TOKEN_RESPONSE_TYPE_INVALID
                                    .withEndpointPrefix());
                    throw new OAuthErrorResponseException(
                            HttpStatusCode.FORBIDDEN,
                            ErrorResponse
                                    .TOKEN_ENDPOINT_RETURNED_JWT_WITH_UNEXPECTED_VALUES_IN_RESPONSE);
                } else {
                    eventProbe.counterMetric(
                            ThirdPartyAPIEndpointMetric.TOKEN_RESPONSE_TYPE_VALID
                                    .withEndpointPrefix());
                }

                return response;
            } catch (JsonProcessingException e) {
                LOGGER.error("JsonProcessingException mapping {} response", REQUEST_NAME);
                LOGGER.debug(e.getMessage());

                eventProbe.counterMetric(
                        ThirdPartyAPIEndpointMetric.TOKEN_RESPONSE_TYPE_INVALID
                                .withEndpointPrefix());

                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_MAP_TOKEN_ENDPOINT_RESPONSE_BODY);
            }
        } else {
            // The token request responded but with an unexpected status code
            LOGGER.error(
                    "{} response status code {} content - {}",
                    REQUEST_NAME,
                    httpReply.statusCode,
                    httpReply.responseBody);

            eventProbe.counterMetric(
                    ThirdPartyAPIEndpointMetric.TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS
                            .withEndpointPrefix());

            if (alertStatusCodes.contains(httpReply.statusCode)) {
                LOGGER.warn("Status code {}, triggered alert metric", httpReply.statusCode);
            }

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.ERROR_TOKEN_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);
        }
    }

    private TokenItem getTokenItemFromTable() {
        return dataStore.getItem(TOKEN_ITEM_ID);
    }

    private void saveTokenItem(TokenItem tokenItem) {
        // id=TOKEN_ITEM_ID as same TokenItem is always used
        tokenItem.setId(TOKEN_ITEM_ID);

        long ttlSeconds = Instant.now().plusSeconds(TOKEN_ITEM_TTL_SECS).getEpochSecond();

        tokenItem.setTtl(ttlSeconds);
        // Create calls put which overwrites any existing token
        dataStore.create(tokenItem);

        LOGGER.info(
                "Token cached - expires {} UTC",
                Instant.ofEpochSecond(ttlSeconds).atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    private URI selectRequestURI(TestStrategyClientId thirdPartyRouting) {
        URI requestUri = null;
        switch (thirdPartyRouting) {
            case STUB:
                requestUri =
                        URI.create(crosscoreV2Configuration.getTokenEndpointURIs().get("STUB"));
                break;
            case UAT:
                requestUri = URI.create(crosscoreV2Configuration.getTokenEndpointURIs().get("UAT"));
                break;
            case LIVE:
                requestUri =
                        URI.create(crosscoreV2Configuration.getTokenEndpointURIs().get("LIVE"));
                break;
            case NO_CHANGE:
                requestUri = URI.create(crosscoreV2Configuration.getTokenEndpoint());
                break;
            default:
                LOGGER.warn(
                        "could not select valid tokenRequestUri falling back to environment default");
                requestUri = URI.create(crosscoreV2Configuration.getTokenEndpoint());
                break;
        }
        return requestUri;
    }

    public boolean isTokenNearExpiration(TokenItem tokenItem, long expiryWindow) {

        if (expiryWindow <= 0 || expiryWindow >= TOKEN_ITEM_TTL_SECS) {
            throw new TokenExpiryWindowException(INVALID_EXPIRY_WINDOW_ERROR_MESSAGE);
        }

        long expiresTime = tokenItem.getTtl();

        long now = Instant.now().getEpochSecond();

        long windowStart =
                Instant.ofEpochSecond(expiresTime).minusSeconds(expiryWindow).getEpochSecond();

        return now >= windowStart;
    }
}
