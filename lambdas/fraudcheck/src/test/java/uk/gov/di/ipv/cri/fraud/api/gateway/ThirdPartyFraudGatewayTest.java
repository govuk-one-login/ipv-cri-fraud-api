package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
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
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.check.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.IdentityVerificationRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.TestStrategyClientId;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.service.CrosscoreV2Configuration;
import uk.gov.di.ipv.cri.fraud.api.service.FraudCheckConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.FraudCheckHttpRetryStatusConfig;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.fraud.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.fraud.library.util.HTTPReply;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse.ERROR_FRAUD_CHECK_RETURNED_UNEXPECTED_HTTP_STATUS_CODE;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_FRAUD_RESPONSE_LATENCY_MILLIS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.FRAUD_RESPONSE_TYPE_VALID;

@ExtendWith(MockitoExtension.class)
class ThirdPartyFraudGatewayTest {

    private static final String TEST_API_RESPONSE_BODY = "test-api-response-content";
    private static final String TEST_ENDPOINT_URL = "https://test-endpoint.co.uk";
    private static final String TEST_ACCESS_TOKEN = "testTokenValue";
    private static final String HMAC_OF_REQUEST_BODY = "hmac-of-request-body";

    private ThirdPartyFraudGateway thirdPartyFraudGateway;

    @Mock private HttpRetryer mockHttpRetryer;
    @Mock private FraudCheckConfigurationService fraudCheckConfigurationService;
    @Mock private CrosscoreV2Configuration mockCrosscoreV2Configuration;
    @Mock private IdentityVerificationRequestMapper mockRequestMapper;
    @Mock private IdentityVerificationResponseMapper mockResponseMapper;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private HmacGenerator mockHmacGenerator;
    @Mock private EventProbe mockEventProbe;

    @BeforeEach
    void setUp() {
        this.thirdPartyFraudGateway =
                new ThirdPartyFraudGateway(
                        mockHttpRetryer,
                        mockRequestMapper,
                        mockResponseMapper,
                        mockObjectMapper,
                        fraudCheckConfigurationService,
                        mockEventProbe);
    }

    @Test
    void shouldInvokeExperianCrosscoreV2Api() throws IOException, OAuthErrorResponseException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final IdentityVerificationRequest testApiRequest = new IdentityVerificationRequest();

        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        IdentityVerificationResponse testResponse = new IdentityVerificationResponse();
        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        when(fraudCheckConfigurationService.getCrosscoreV2Configuration())
                .thenReturn(mockCrosscoreV2Configuration);
        when(mockCrosscoreV2Configuration.getTenantId()).thenReturn("12345");
        when(mockCrosscoreV2Configuration.getEndpointUri()).thenReturn("http://localhost");
        when(mockRequestMapper.mapPersonIdentity(personIdentity, "12345"))
                .thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(FraudCheckHttpRetryStatusConfig.class),
                        eq("Fraud Check")))
                .thenReturn(new HTTPReply(200, null, TEST_API_RESPONSE_BODY));

        when(this.mockObjectMapper.readValue(
                        TEST_API_RESPONSE_BODY, IdentityVerificationResponse.class))
                .thenReturn(testResponse);
        when(this.mockResponseMapper.mapFraudResponse(testResponse))
                .thenReturn(testFraudCheckResult);

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(
                        personIdentity, TEST_ACCESS_TOKEN, TestStrategyClientId.NO_CHANGE);

        InOrder inOrderMockEventProbe = inOrder(mockEventProbe);
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(FRAUD_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(FRAUD_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(eq(THIRD_PARTY_FRAUD_RESPONSE_LATENCY_MILLIS), anyDouble());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(FRAUD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(FRAUD_RESPONSE_TYPE_VALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        verify(mockRequestMapper).mapPersonIdentity(personIdentity, "12345");
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHttpRetryer)
                .sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(FraudCheckHttpRetryStatusConfig.class),
                        eq("Fraud Check"));
        verify(mockResponseMapper).mapFraudResponse(testResponse);
        assertNotNull(actualFraudCheckResult);
        assertEquals(HttpPost.class, httpRequestCaptor.getValue().getClass());
        assertHeaders(httpRequestCaptor);
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionIfThirdPartyApiReturnsInvalidCrosscoreV2Response()
            throws IOException, OAuthErrorResponseException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final IdentityVerificationRequest testApiRequest = new IdentityVerificationRequest();

        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);

        when(mockRequestMapper.mapPersonIdentity(personIdentity, "12345"))
                .thenReturn(testApiRequest);
        when(fraudCheckConfigurationService.getCrosscoreV2Configuration())
                .thenReturn(mockCrosscoreV2Configuration);
        when(mockCrosscoreV2Configuration.getTenantId()).thenReturn("12345");
        when(mockCrosscoreV2Configuration.getEndpointUri()).thenReturn("http://localhost");

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(FraudCheckHttpRetryStatusConfig.class),
                        eq("Fraud Check")))
                .thenReturn(new HTTPReply(200, null, "}BAD JSON{"));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_MAP_FRAUD_CHECK_RESPONSE_BODY);

        // Trigger the mapping failure via the mock
        when(mockObjectMapper.readValue("}BAD JSON{", IdentityVerificationResponse.class))
                .thenThrow(
                        new InputCoercionException(
                                null, "Problem during json mapping", null, null));

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                thirdPartyFraudGateway.performFraudCheck(
                                        personIdentity,
                                        TEST_ACCESS_TOKEN,
                                        TestStrategyClientId.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());

        InOrder inOrderMockEventProbe = inOrder(mockEventProbe);
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(FRAUD_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(FRAUD_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(eq(THIRD_PARTY_FRAUD_RESPONSE_LATENCY_MILLIS), anyDouble());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(FRAUD_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(FRAUD_RESPONSE_TYPE_INVALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        verify(mockRequestMapper).mapPersonIdentity(personIdentity, "12345");
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHttpRetryer)
                .sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(FraudCheckHttpRetryStatusConfig.class),
                        eq("Fraud Check"));

        assertEquals(HttpPost.class, httpRequestCaptor.getValue().getClass());
        assertHeaders(httpRequestCaptor);
    }

    @ParameterizedTest
    @CsvSource({
        "300", "400", "500", "-1",
    })
    void thirdPartyApiReturnsErrorOnUnexpectedHTTPStatusCrosscoreV2Response(int errorStatus)
            throws IOException, OAuthErrorResponseException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final IdentityVerificationRequest testApiRequest = new IdentityVerificationRequest();

        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        when(mockRequestMapper.mapPersonIdentity(personIdentity, "12345"))
                .thenReturn(testApiRequest);
        when(fraudCheckConfigurationService.getCrosscoreV2Configuration())
                .thenReturn(mockCrosscoreV2Configuration);
        when(mockCrosscoreV2Configuration.getTenantId()).thenReturn("12345");
        when(mockCrosscoreV2Configuration.getEndpointUri()).thenReturn("http://localhost");

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(FraudCheckHttpRetryStatusConfig.class),
                        eq("Fraud Check")))
                .thenReturn(new HTTPReply(errorStatus, null, TEST_API_RESPONSE_BODY));

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(
                        personIdentity, TEST_ACCESS_TOKEN, TestStrategyClientId.NO_CHANGE);

        InOrder inOrderMockEventProbe = inOrder(mockEventProbe);
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(FRAUD_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(FRAUD_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(eq(THIRD_PARTY_FRAUD_RESPONSE_LATENCY_MILLIS), anyDouble());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(FRAUD_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        final String EXPECTED_ERROR =
                ERROR_FRAUD_CHECK_RETURNED_UNEXPECTED_HTTP_STATUS_CODE.getMessage();

        verify(mockRequestMapper).mapPersonIdentity(personIdentity, "12345");
        verify(mockObjectMapper).writeValueAsString(testApiRequest);

        verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(FraudCheckHttpRetryStatusConfig.class),
                        eq("Fraud Check"));

        assertNotNull(actualFraudCheckResult);
        assertEquals(EXPECTED_ERROR, actualFraudCheckResult.getErrorMessage());
        assertEquals(HttpPost.class, httpRequestCaptor.getValue().getClass());
        assertHeaders(httpRequestCaptor);
    }

    private void assertHeaders(ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor) {
        // Check Headers
        Map<String, String> httpHeadersKV =
                Arrays.stream(httpRequestCaptor.getValue().getAllHeaders())
                        .collect(Collectors.toMap(Header::getName, Header::getValue));

        assertNotNull(httpHeadersKV.get("Content-Type"));
        assertEquals("application/json", httpHeadersKV.get("Content-Type"));

        assertNotNull(httpHeadersKV.get("Accept"));
        assertEquals("application/json", httpHeadersKV.get("Accept"));
        assertNotNull(httpHeadersKV.get("Authorization"));
        assertEquals("Bearer " + TEST_ACCESS_TOKEN, httpHeadersKV.get("Authorization"));
    }
}
