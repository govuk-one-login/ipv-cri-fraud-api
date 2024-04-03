package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.TestStrategyClientId;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.TokenResponse;
import uk.gov.di.ipv.cri.fraud.api.persistence.item.TokenItem;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.fraud.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.fraud.library.util.HTTPReply;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.fraud.api.service.TokenRequestService.TOKEN_ITEM_ID;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_REQUEST_REUSING_CACHED_TOKEN;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_REQUEST_SEND_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_RESPONSE_FAILED_TO_GENERATE_NEW_TOKEN_METRIC;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.TOKEN_RESPONSE_TYPE_VALID;

@ExtendWith(MockitoExtension.class)
class TokenRequestServiceTest {

    private static final String TEST_END_POINT = "http://127.0.0.1";
    private static final String TEST_TOKEN_TABLE_NAME = "test_token_table_name";

    // needs to be legitimate JWT to pass validation
    private static final String TEST_TOKEN_ISSUER = "test-token-issuer";
    private static final String TEST_TOKEN_VALID_VALUE =
            "eyJraWQiOiJJSmpTMXJQQjdJODBHWjgybmNsSlZPQkF3V3B3ZTVYblNKZUdSZHdpcEY5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.ewogICJzdWIiOiAiVEVTVCIsCiAgIkVtYWlsIjogbnVsbCwKICAiRmlyc3ROYW1lIjogbnVsbCwKICAiaXNzIjogInRlc3QtdG9rZW4taXNzdWVyIiwKICAiTGFzdE5hbWUiOiBudWxsLAogICJleHAiOiAxNzA2MjgwOTIyLAogICJpYXQiOiAxNzA2Mjc5MTIyLAogICJqdGkiOiAiNzM5MjQzODktYWI5Yy00Y2MxLWJkZGMtZTA4NzJjMmExMmZkIgp9.u1k1phRJNm9Sg9P5SF83NDk0XpcQaLXqVrfxFR9MtctzFzgTFyLwphx94OL7mVtPOzKiHm-Id4wwaFX56kGEO3zMoZ6nH8YTlAiUD_Cg8V_XqNNONuEm1iGEKl_GbRQHmCE3QiFaTc64eSzJ81zrotRsSjDatbDRULXajDl7VmRhYq7TODxqKpVWxXdEgyuNPLOECW3sfVg7hLj5CPGpW-G6yvR6gvEJERGRukCtaHdp4Zj4uZt-3VgLSrkFcRimek4sqQkq0uLv6wfYrnTxjrAv3c982ElsVKjyhR65qToZSzMDXI2o8-4GMcKX5EdpC9TUKvwrBVbwgZeHVNYvgQ";
    private static final String TEST_TOKEN_INVALID_ALG =
            "ewogICJraWQiOiAiSUpqUzFyUEI3STgwR1o4Mm5jbEpWT0JBd1dwd2U1WG5TSmVHUmR3aXBGOSIsCiAgInR5cCI6ICJKV1QiLAogICJhbGciOiAibm9uZSIKfQ==.ewogICJzdWIiOiAiVEVTVCIsCiAgIkVtYWlsIjogbnVsbCwKICAiRmlyc3ROYW1lIjogbnVsbCwKICAiaXNzIjogInRlc3QtdG9rZW4taXNzdWVyIiwKICAiTGFzdE5hbWUiOiBudWxsLAogICJleHAiOiAxNzA2MjgwOTIyLAogICJpYXQiOiAxNzA2Mjc5MTIyLAogICJqdGkiOiAiNzM5MjQzODktYWI5Yy00Y2MxLWJkZGMtZTA4NzJjMmExMmZkIgp9.u1k1phRJNm9Sg9P5SF83NDk0XpcQaLXqVrfxFR9MtctzFzgTFyLwphx94OL7mVtPOzKiHm-Id4wwaFX56kGEO3zMoZ6nH8YTlAiUD_Cg8V_XqNNONuEm1iGEKl_GbRQHmCE3QiFaTc64eSzJ81zrotRsSjDatbDRULXajDl7VmRhYq7TODxqKpVWxXdEgyuNPLOECW3sfVg7hLj5CPGpW-G6yvR6gvEJERGRukCtaHdp4Zj4uZt-3VgLSrkFcRimek4sqQkq0uLv6wfYrnTxjrAv3c982ElsVKjyhR65qToZSzMDXI2o8-4GMcKX5EdpC9TUKvwrBVbwgZeHVNYvgQ";
    private static final String TEST_TOKEN_INVALID_ISS =
            "eyJraWQiOiJJSmpTMXJQQjdJODBHWjgybmNsSlZPQkF3V3B3ZTVYblNKZUdSZHdpcEY5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.ewogICJzdWIiOiAiVEVTVCIsCiAgIkVtYWlsIjogbnVsbCwKICAiRmlyc3ROYW1lIjogbnVsbCwKICAiaXNzIjogIkpCIiwKICAiTGFzdE5hbWUiOiBudWxsLAogICJleHAiOiAxNzA2MjgwOTIyLAogICJpYXQiOiAxNzA2Mjc5MTIyLAogICJqdGkiOiAiNzM5MjQzODktYWI5Yy00Y2MxLWJkZGMtZTA4NzJjMmExMmZkIgp9.u1k1phRJNm9Sg9P5SF83NDk0XpcQaLXqVrfxFR9MtctzFzgTFyLwphx94OL7mVtPOzKiHm-Id4wwaFX56kGEO3zMoZ6nH8YTlAiUD_Cg8V_XqNNONuEm1iGEKl_GbRQHmCE3QiFaTc64eSzJ81zrotRsSjDatbDRULXajDl7VmRhYq7TODxqKpVWxXdEgyuNPLOECW3sfVg7hLj5CPGpW-G6yvR6gvEJERGRukCtaHdp4Zj4uZt-3VgLSrkFcRimek4sqQkq0uLv6wfYrnTxjrAv3c982ElsVKjyhR65qToZSzMDXI2o8-4GMcKX5EdpC9TUKvwrBVbwgZeHVNYvgQ";
    private static final String TEST_TOKEN_INVALID_SUB =
            "eyJraWQiOiJJSmpTMXJQQjdJODBHWjgybmNsSlZPQkF3V3B3ZTVYblNKZUdSZHdpcEY5IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.ewogICJzdWIiOiAid3JvbmdVc2VybmFtZSIsCiAgIkVtYWlsIjogbnVsbCwKICAiRmlyc3ROYW1lIjogbnVsbCwKICAiaXNzIjogInRlc3QtdG9rZW4taXNzdWVyIiwKICAiTGFzdE5hbWUiOiBudWxsLAogICJleHAiOiAxNzA2MjgwOTIyLAogICJpYXQiOiAxNzA2Mjc5MTIyLAogICJqdGkiOiAiNzM5MjQzODktYWI5Yy00Y2MxLWJkZGMtZTA4NzJjMmExMmZkIgp9.u1k1phRJNm9Sg9P5SF83NDk0XpcQaLXqVrfxFR9MtctzFzgTFyLwphx94OL7mVtPOzKiHm-Id4wwaFX56kGEO3zMoZ6nH8YTlAiUD_Cg8V_XqNNONuEm1iGEKl_GbRQHmCE3QiFaTc64eSzJ81zrotRsSjDatbDRULXajDl7VmRhYq7TODxqKpVWxXdEgyuNPLOECW3sfVg7hLj5CPGpW-G6yvR6gvEJERGRukCtaHdp4Zj4uZt-3VgLSrkFcRimek4sqQkq0uLv6wfYrnTxjrAv3c982ElsVKjyhR65qToZSzMDXI2o8-4GMcKX5EdpC9TUKvwrBVbwgZeHVNYvgQ";
    private static final String TEST_TOKEN_INVALID_ALG_ISS_SUB =
            "ewogICJraWQiOiAiSUpqUzFyUEI3STgwR1o4Mm5jbEpWT0JBd1dwd2U1WG5TSmVHUmR3aXBGOSIsCiAgInR5cCI6ICJKV1QiLAogICJhbGciOiAibm9uZSIKfQ==.ewogICJzdWIiOiAid3JvbmdVc2VybmFtZSIsCiAgIkVtYWlsIjogbnVsbCwKICAiRmlyc3ROYW1lIjogbnVsbCwKICAiaXNzIjogIkpCIiwKICAiTGFzdE5hbWUiOiBudWxsLAogICJleHAiOiAxNzA2MjgwOTIyLAogICJpYXQiOiAxNzA2Mjc5MTIyLAogICJqdGkiOiAiNzM5MjQzODktYWI5Yy00Y2MxLWJkZGMtZTA4NzJjMmExMmZkIgp9.u1k1phRJNm9Sg9P5SF83NDk0XpcQaLXqVrfxFR9MtctzFzgTFyLwphx94OL7mVtPOzKiHm-Id4wwaFX56kGEO3zMoZ6nH8YTlAiUD_Cg8V_XqNNONuEm1iGEKl_GbRQHmCE3QiFaTc64eSzJ81zrotRsSjDatbDRULXajDl7VmRhYq7TODxqKpVWxXdEgyuNPLOECW3sfVg7hLj5CPGpW-G6yvR6gvEJERGRukCtaHdp4Zj4uZt-3VgLSrkFcRimek4sqQkq0uLv6wfYrnTxjrAv3c982ElsVKjyhR65qToZSzMDXI2o8-4GMcKX5EdpC9TUKvwrBVbwgZeHVNYvgQ";
    private static final String TEST_USER_DOMAIN = "domain_test_value";
    private static final String TEST_USER_NAME = "TEST";
    private static final String TEST_PASSWORD = "PASSWORD";
    private static String expectedContentTypeTestHeaderKey = "X-CONTENT-TYPE-TEST-HEADER";
    private String expectedContentTypeTestHeaderValue = "content_type_test_value";

    private static String expectedCorrelationIdTestHeaderKey = "X-CORRELATIONID-TEST-HEADER";
    private String expectedCorrelationIdTestHeaderValue = "correlationId_test_value";
    private static String expectedDomainTestHeaderKey = "X-DOMAIN-TEST-HEADER";
    private String expectedDomainHeaderValue = "domain_test_value";
    private static final String expectedClientId = "test-clientId";
    private static final String expectedClientSecret = "test-clientSecret";

    @Mock CrosscoreV2Configuration mockCrosscoreV2Configuration;
    @Mock DynamoDbEnhancedClient mockDynamoDbEnhancedClient;
    @Mock HttpRetryer mockHttpRetryer;
    @Mock private RequestConfig mockRequestConfig;
    private ObjectMapper realObjectMapper;
    @Mock private EventProbe mockEventProbe;

    private TokenRequestService tokenRequestService;

    @Mock DynamoDbTable<TokenItem> mockTokenTable; // To mock the internals of Datastore
    private final Key TOKEN_ITEM_KEY = Key.builder().partitionValue(TOKEN_ITEM_ID).build();

    @BeforeEach
    void setUp() {
        realObjectMapper = new ObjectMapper();

        when(mockCrosscoreV2Configuration.getTokenEndpoint()).thenReturn(TEST_END_POINT);
        when(mockCrosscoreV2Configuration.getTokenTableName()).thenReturn(TEST_TOKEN_TABLE_NAME);
        when(mockCrosscoreV2Configuration.getUsername()).thenReturn(TEST_USER_NAME);
        when(mockCrosscoreV2Configuration.getPassword()).thenReturn(TEST_PASSWORD);
        when(mockCrosscoreV2Configuration.getClientId()).thenReturn(expectedClientId);
        when(mockCrosscoreV2Configuration.getClientSecret()).thenReturn(expectedClientSecret);
        when(mockCrosscoreV2Configuration.getUserDomain()).thenReturn(TEST_USER_DOMAIN);

        // Datastore is wrapper around DynamoDbEnhancedClient
        when(mockDynamoDbEnhancedClient.table(eq(TEST_TOKEN_TABLE_NAME), any(TableSchema.class)))
                .thenReturn(mockTokenTable);

        tokenRequestService =
                new TokenRequestService(
                        mockCrosscoreV2Configuration,
                        mockDynamoDbEnhancedClient,
                        mockHttpRetryer,
                        mockRequestConfig,
                        realObjectMapper,
                        mockEventProbe);
    }

    @Test
    void shouldReturnTokenValueWhenTokenEndpointRespondsWithToken()
            throws OAuthErrorResponseException, IOException {
        when(mockCrosscoreV2Configuration.getTokenIssuer()).thenReturn(TEST_TOKEN_ISSUER);

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        TokenResponse testTokenResponse =
                TokenResponse.builder().accessToken(TEST_TOKEN_VALID_VALUE).build();
        String testTokenResponseString = realObjectMapper.writeValueAsString(testTokenResponse);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(TokenHttpRetryStatusConfig.class),
                        eq("Token")))
                .thenReturn(new HTTPReply(200, null, testTokenResponseString));

        String tokenValue = tokenRequestService.requestToken(false, TestStrategyClientId.NO_CHANGE);

        // (POST) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class), anyString());
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_VALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertNotNull(tokenValue);
        assertEquals(TEST_TOKEN_VALID_VALUE, tokenValue);
        // Check Headers
        assertTokenHeaders(httpRequestCaptor);
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenTokenEndpointDoesNotRespond()
            throws IOException, OAuthErrorResponseException {

        Exception exceptionCaught = new IOException("Token Endpoint Timed out");

        doThrow(exceptionCaught)
                .when(mockHttpRetryer)
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class), anyString());

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.ERROR_INVOKING_THIRD_PARTY_API_TOKEN_ENDPOINT);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                tokenRequestService.requestToken(
                                        true, TestStrategyClientId.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        // (Post) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class), anyString());
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        TOKEN_REQUEST_SEND_ERROR.withEndpointPrefixAndExceptionName(
                                exceptionCaught));
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        TOKEN_RESPONSE_FAILED_TO_GENERATE_NEW_TOKEN_METRIC.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenTokenEndpointResponseStatusCodeNot200()
            throws IOException, OAuthErrorResponseException {
        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(TokenHttpRetryStatusConfig.class),
                        eq("Token")))
                .thenReturn(new HTTPReply(501, null, "Server Error"));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.ERROR_TOKEN_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                tokenRequestService.requestToken(
                                        true, TestStrategyClientId.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        // (Post) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class), anyString());
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        TOKEN_RESPONSE_FAILED_TO_GENERATE_NEW_TOKEN_METRIC.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @ParameterizedTest
    @CsvSource({
        "400", "401",
    })
    void shouldCaptureTokenResponseStatusCodeAlertMetricWhenStatusCodeIs(
            int tokenResponseStatusCode) throws IOException, OAuthErrorResponseException {
        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(TokenHttpRetryStatusConfig.class),
                        anyString()))
                .thenReturn(new HTTPReply(tokenResponseStatusCode, null, "Server Error"));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.ERROR_TOKEN_ENDPOINT_RETURNED_UNEXPECTED_HTTP_STATUS_CODE);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                tokenRequestService.requestToken(
                                        true, TestStrategyClientId.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        // (Post) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class), anyString());
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        TOKEN_RESPONSE_FAILED_TO_GENERATE_NEW_TOKEN_METRIC.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionWhenFailingToMapTokenEndpointResponse()
            throws IOException, OAuthErrorResponseException {
        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);
        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(TokenHttpRetryStatusConfig.class),
                        anyString()))
                .thenReturn(new HTTPReply(200, null, "not-json"));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_MAP_TOKEN_ENDPOINT_RESPONSE_BODY);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                tokenRequestService.requestToken(
                                        true, TestStrategyClientId.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        // (Post) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class), anyString());
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_INVALID.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        TOKEN_RESPONSE_FAILED_TO_GENERATE_NEW_TOKEN_METRIC.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldReturnCachedAccessTokenIfTokenNotExpired()
            throws IOException, OAuthErrorResponseException {

        when(mockCrosscoreV2Configuration.getTokenIssuer()).thenReturn(TEST_TOKEN_ISSUER);

        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        // Captor used as TTL is set and expiry enforced by the CRI
        ArgumentCaptor<TokenItem> dynamoPutItemTokenItemCaptor =
                ArgumentCaptor.forClass(TokenItem.class);

        // Bearer access token
        TokenResponse testTokenResponse =
                TokenResponse.builder().accessToken(TEST_TOKEN_VALID_VALUE).build();
        String testTokenResponseString = realObjectMapper.writeValueAsString(testTokenResponse);
        // Request one
        when(mockTokenTable.getItem(TOKEN_ITEM_KEY)).thenReturn(null);
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(TokenHttpRetryStatusConfig.class),
                        anyString()))
                .thenReturn(new HTTPReply(200, null, testTokenResponseString));
        // Token put capture
        doNothing().when(mockTokenTable).putItem(dynamoPutItemTokenItemCaptor.capture());
        String tokenResponseOne =
                tokenRequestService.requestToken(false, TestStrategyClientId.NO_CHANGE);
        assertEquals(TEST_TOKEN_VALID_VALUE, tokenResponseOne);

        // Request two
        TokenItem testTokenFromDynamo = dynamoPutItemTokenItemCaptor.getValue();
        // Captured token get
        when(mockTokenTable.getItem(TOKEN_ITEM_KEY)).thenReturn(testTokenFromDynamo);
        String tokenResponseTwo =
                tokenRequestService.requestToken(false, TestStrategyClientId.NO_CHANGE);

        assertEquals(tokenResponseOne, tokenResponseTwo);

        // (Post) Token - Only one send invocation intended
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class), anyString());
        verifyNoMoreInteractions(mockHttpRetryer);

        // Times 1 here is important - token is cached
        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_RESPONSE_TYPE_VALID.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_REQUEST_REUSING_CACHED_TOKEN.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);
    }

    @Test
    void shouldRequestNewAccessTokenIfCachedTokenIsExpired()
            throws IOException, OAuthErrorResponseException {

        when(mockCrosscoreV2Configuration.getTokenIssuer()).thenReturn(TEST_TOKEN_ISSUER);
        ArgumentCaptor<HttpPost> httpRequestCaptor = ArgumentCaptor.forClass(HttpPost.class);

        // Captor used as TTL is set and expiry enforced by the CRI
        ArgumentCaptor<TokenItem> dynamoPutItemTokenItemCaptor =
                ArgumentCaptor.forClass(TokenItem.class);

        // Bearer access token
        TokenResponse testTokenResponse =
                TokenResponse.builder().accessToken(TEST_TOKEN_VALID_VALUE).build();
        String testTokenResponseString = realObjectMapper.writeValueAsString(testTokenResponse);

        // Request one
        when(mockTokenTable.getItem(TOKEN_ITEM_KEY)).thenReturn(null);
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(TokenHttpRetryStatusConfig.class),
                        anyString()))
                .thenReturn(new HTTPReply(200, null, testTokenResponseString));
        // Token put capture
        doNothing().when(mockTokenTable).putItem(dynamoPutItemTokenItemCaptor.capture());
        String tokenResponseOne =
                tokenRequestService.requestToken(false, TestStrategyClientId.NO_CHANGE);
        assertEquals(TEST_TOKEN_VALID_VALUE, tokenResponseOne);

        // Request two
        TokenItem testTokenFromDynamo = dynamoPutItemTokenItemCaptor.getValue();

        // Overriding the TokenItem TTL so it is expired and we should then make a new request
        testTokenFromDynamo.setTtl(Instant.now().getEpochSecond());

        // Captured token get
        when(mockTokenTable.getItem(TOKEN_ITEM_KEY)).thenReturn(testTokenFromDynamo);
        String tokenResponseTwo =
                tokenRequestService.requestToken(false, TestStrategyClientId.NO_CHANGE);

        assertEquals(tokenResponseOne, tokenResponseTwo);

        // (Post) Token - Two send invocations intended
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(2))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class), anyString());
        verifyNoMoreInteractions(mockHttpRetryer);

        // Times 1 here is important - token is cached
        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        // Request one
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_RESPONSE_TYPE_VALID.withEndpointPrefix());
        // Request Two
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe, times(1))
                .counterMetric(TOKEN_RESPONSE_TYPE_VALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);
    }

    @Test
    void shouldOAuthErrorResponseExceptionWhenTokenJWTContainsNoAlg()
            throws OAuthErrorResponseException, IOException {

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        TokenResponse testTokenResponse =
                TokenResponse.builder().accessToken(TEST_TOKEN_INVALID_ALG).build();
        String testTokenResponseString = realObjectMapper.writeValueAsString(testTokenResponse);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(TokenHttpRetryStatusConfig.class),
                        eq("Token")))
                .thenReturn(new HTTPReply(200, null, testTokenResponseString));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_FORBIDDEN,
                        ErrorResponse
                                .TOKEN_ENDPOINT_RETURNED_JWT_WITH_UNEXPECTED_VALUES_IN_RESPONSE);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                tokenRequestService.requestToken(
                                        true, TestStrategyClientId.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        // (POST) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class), anyString());
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_INVALID.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        TOKEN_RESPONSE_FAILED_TO_GENERATE_NEW_TOKEN_METRIC.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);
        // Check Headers
        assertTokenHeaders(httpRequestCaptor);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldOAuthErrorResponseExceptionWhenTokenJWTContainsInvalidIssuer()
            throws OAuthErrorResponseException, IOException {
        when(mockCrosscoreV2Configuration.getTokenIssuer()).thenReturn(TEST_TOKEN_ISSUER);

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        TokenResponse testTokenResponse =
                TokenResponse.builder().accessToken(TEST_TOKEN_INVALID_ISS).build();
        String testTokenResponseString = realObjectMapper.writeValueAsString(testTokenResponse);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(TokenHttpRetryStatusConfig.class),
                        eq("Token")))
                .thenReturn(new HTTPReply(200, null, testTokenResponseString));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_FORBIDDEN,
                        ErrorResponse
                                .TOKEN_ENDPOINT_RETURNED_JWT_WITH_UNEXPECTED_VALUES_IN_RESPONSE);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                tokenRequestService.requestToken(
                                        true, TestStrategyClientId.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        // (POST) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class), anyString());
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_INVALID.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        TOKEN_RESPONSE_FAILED_TO_GENERATE_NEW_TOKEN_METRIC.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);
        // Check Headers
        assertTokenHeaders(httpRequestCaptor);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    @Test
    void shouldOAuthErrorResponseExceptionWhenTokenJWTContainsInvalidSub()
            throws OAuthErrorResponseException, IOException {

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        TokenResponse testTokenResponse =
                TokenResponse.builder().accessToken(TEST_TOKEN_INVALID_SUB).build();
        String testTokenResponseString = realObjectMapper.writeValueAsString(testTokenResponse);

        // HttpClient response
        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(TokenHttpRetryStatusConfig.class),
                        eq("Token")))
                .thenReturn(new HTTPReply(200, null, testTokenResponseString));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_FORBIDDEN,
                        ErrorResponse
                                .TOKEN_ENDPOINT_RETURNED_JWT_WITH_UNEXPECTED_VALUES_IN_RESPONSE);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                tokenRequestService.requestToken(
                                        true, TestStrategyClientId.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        // (POST) Token
        InOrder inOrderMockHttpRetryerSequence = inOrder(mockHttpRetryer);
        inOrderMockHttpRetryerSequence
                .verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        any(HttpPost.class), any(TokenHttpRetryStatusConfig.class), anyString());
        verifyNoMoreInteractions(mockHttpRetryer);

        InOrder inOrderMockEventProbeSequence = inOrder(mockEventProbe);
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(TOKEN_RESPONSE_TYPE_INVALID.withEndpointPrefix());
        inOrderMockEventProbeSequence
                .verify(mockEventProbe)
                .counterMetric(
                        TOKEN_RESPONSE_FAILED_TO_GENERATE_NEW_TOKEN_METRIC.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);
        // Check Headers
        assertTokenHeaders(httpRequestCaptor);

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());
    }

    private void assertTokenHeaders(
            ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor) {
        // Check Headers
        Map<String, String> httpHeadersKV =
                Arrays.stream(httpRequestCaptor.getValue().getAllHeaders())
                        .collect(Collectors.toMap(Header::getName, Header::getValue));

        assertNotNull(httpHeadersKV.get("Content-Type"));
        assertEquals("application/json", httpHeadersKV.get("Content-Type"));
    }
}
