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
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.check.PepCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.PEPRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.PEPResponse;
import uk.gov.di.ipv.cri.fraud.api.service.CrosscoreV2Configuration;
import uk.gov.di.ipv.cri.fraud.api.service.FraudCheckConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.PepCheckHttpRetryStatusConfig;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.fraud.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.fraud.library.strategy.Strategy;
import uk.gov.di.ipv.cri.fraud.library.util.HTTPReply;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse.ERROR_PEP_CHECK_RETURNED_UNEXPECTED_HTTP_STATUS_CODE;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_RESPONSE_TYPE_INVALID;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_RESPONSE_TYPE_VALID;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.THIRD_PARTY_PEP_RESPONSE_LATENCY;

@ExtendWith(MockitoExtension.class)
class ThirdPartyPepGatewayTest {

    private static final String TEST_API_RESPONSE_BODY = "test-api-response-content";
    private static final String TEST_ENDPOINT_URL = "https://test-endpoint.co.uk";
    private static final String TEST_ACCESS_TOKEN = "testTokenValue";
    private static final String HMAC_OF_REQUEST_BODY = "hmac-of-request-body";
    String stubExperianEndpointValue = "http://localhostStub";
    String UatExperianEndpointValue = "http://localhostUat";
    String LiveExperianEndpointValue = "http://localhostLive";
    String stubTokenEndpointValue = "http://localhostStub";
    String UatTokenEndpointValue = "http://localhostUat";
    String LiveTokenEndpointValue = "http://localhostLive";
    private ThirdPartyPepGateway thirdPartyPepGateway;

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
        thirdPartyPepGateway =
                new ThirdPartyPepGateway(
                        mockHttpRetryer,
                        mockRequestMapper,
                        mockResponseMapper,
                        mockObjectMapper,
                        fraudCheckConfigurationService,
                        mockEventProbe);
    }

    @ParameterizedTest
    @EnumSource(Strategy.class)
    void shouldInvokeCrosscoreV2PepApi(Strategy strategy)
            throws IOException, OAuthErrorResponseException {
        final String testRequestBody = "serialisedPepApiRequest";

        final PEPRequest testApiRequest = createMockAPIRequest();

        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        PEPResponse testPepResponse = new PEPResponse();
        PepCheckResult testPepCheckResult = new PepCheckResult();
        when(fraudCheckConfigurationService.getCrosscoreV2Configuration())
                .thenReturn(mockCrosscoreV2Configuration);
        when(mockCrosscoreV2Configuration.getTenantId()).thenReturn("12345");

        if (strategy == Strategy.NO_CHANGE) {
            when(mockCrosscoreV2Configuration.getEndpointUri()).thenReturn("http://localhost");
        } else {
            when(mockCrosscoreV2Configuration.getEndpointURIs())
                    .thenReturn(
                            Map.of(
                                    "STUB",
                                    "http://stub",
                                    "UAT",
                                    "http://uat",
                                    "LIVE",
                                    "http://live"));
        }
        when(mockRequestMapper.mapPEPPersonIdentity(personIdentity, "12345"))
                .thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(PepCheckHttpRetryStatusConfig.class),
                        eq("Pep Check")))
                .thenReturn(new HTTPReply(200, null, TEST_API_RESPONSE_BODY));

        when(this.mockObjectMapper.readValue(TEST_API_RESPONSE_BODY, PEPResponse.class))
                .thenReturn(testPepResponse);
        when(this.mockResponseMapper.mapPEPResponse(testPepResponse))
                .thenReturn(testPepCheckResult);

        PepCheckResult actualPepCheckResult =
                thirdPartyPepGateway.performPepCheck(personIdentity, TEST_ACCESS_TOKEN, strategy);

        InOrder inOrderMockEventProbe = inOrder(mockEventProbe);
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(
                        eq(THIRD_PARTY_PEP_RESPONSE_LATENCY.toString().toLowerCase()), anyDouble());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_RESPONSE_TYPE_VALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        verify(mockRequestMapper).mapPEPPersonIdentity(personIdentity, "12345");
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHttpRetryer)
                .sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(PepCheckHttpRetryStatusConfig.class),
                        eq("Pep Check"));
        verify(mockResponseMapper).mapPEPResponse(testPepResponse);

        assertNotNull(actualPepCheckResult);
        assertEquals(HttpPost.class, httpRequestCaptor.getValue().getClass());
        assertHeaders(httpRequestCaptor, true);
    }

    @Test
    void shouldReturnOAuthErrorResponseExceptionIfThirdPartyApiReturnsInvalidCrosscoreV2Response()
            throws IOException, OAuthErrorResponseException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        final PEPRequest testApiRequest = createMockAPIRequest();

        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        when(fraudCheckConfigurationService.getCrosscoreV2Configuration())
                .thenReturn(mockCrosscoreV2Configuration);
        when(mockCrosscoreV2Configuration.getEndpointUri()).thenReturn("http://localhost");
        when(mockCrosscoreV2Configuration.getTenantId()).thenReturn("12345");

        when(mockRequestMapper.mapPEPPersonIdentity(personIdentity, "12345"))
                .thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(PepCheckHttpRetryStatusConfig.class),
                        eq("Pep Check")))
                .thenReturn(new HTTPReply(200, null, "}BAD JSON{"));

        OAuthErrorResponseException expectedReturnedException =
                new OAuthErrorResponseException(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_MAP_PEP_CHECK_RESPONSE_BODY);

        // Trigger the mapping failure via the mock
        when(mockObjectMapper.readValue("}BAD JSON{", PEPResponse.class))
                .thenThrow(
                        new InputCoercionException(
                                null, "Problem during json mapping", null, null));

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () ->
                                thirdPartyPepGateway.performPepCheck(
                                        personIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE),
                        "Expected OAuthErrorResponseException");

        assertEquals(expectedReturnedException.getStatusCode(), thrownException.getStatusCode());
        assertEquals(expectedReturnedException.getErrorReason(), thrownException.getErrorReason());

        InOrder inOrderMockEventProbe = inOrder(mockEventProbe);
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(
                        eq(THIRD_PARTY_PEP_RESPONSE_LATENCY.toString().toLowerCase()), anyDouble());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_RESPONSE_TYPE_INVALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        verify(mockRequestMapper).mapPEPPersonIdentity(personIdentity, "12345");
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHttpRetryer)
                .sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(PepCheckHttpRetryStatusConfig.class),
                        eq("Pep Check"));

        assertEquals(HttpPost.class, httpRequestCaptor.getValue().getClass());
        assertHeaders(httpRequestCaptor, true);
    }

    @ParameterizedTest
    @CsvSource({
        "300", "400", "500", "-1",
    })
    void thirdPartyApiReturnsErrorOnHTTP300CrosscoreV2Response(int errorStatus)
            throws IOException, OAuthErrorResponseException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";

        final PEPRequest testApiRequest = createMockAPIRequest();

        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        when(fraudCheckConfigurationService.getCrosscoreV2Configuration())
                .thenReturn(mockCrosscoreV2Configuration);
        when(mockCrosscoreV2Configuration.getTenantId()).thenReturn("12345");
        when(mockCrosscoreV2Configuration.getEndpointUri()).thenReturn("http://localhost");

        when(mockRequestMapper.mapPEPPersonIdentity(personIdentity, "12345"))
                .thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(PepCheckHttpRetryStatusConfig.class),
                        eq("Pep Check")))
                .thenReturn(new HTTPReply(errorStatus, null, TEST_API_RESPONSE_BODY));

        PepCheckResult actualPepCheckResult =
                thirdPartyPepGateway.performPepCheck(
                        personIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE);

        InOrder inOrderMockEventProbe = inOrder(mockEventProbe);
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(
                        eq(THIRD_PARTY_PEP_RESPONSE_LATENCY.toString().toLowerCase()), anyDouble());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_RESPONSE_TYPE_UNEXPECTED_HTTP_STATUS.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        final String EXPECTED_ERROR =
                ERROR_PEP_CHECK_RETURNED_UNEXPECTED_HTTP_STATUS_CODE.getMessage();

        verify(mockRequestMapper).mapPEPPersonIdentity(personIdentity, "12345");
        verify(mockObjectMapper).writeValueAsString(testApiRequest);

        verify(mockHttpRetryer, times(1))
                .sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(PepCheckHttpRetryStatusConfig.class),
                        eq("Pep Check"));

        assertNotNull(actualPepCheckResult);
        assertEquals(EXPECTED_ERROR, actualPepCheckResult.getErrorMessage());
        assertEquals(HttpPost.class, httpRequestCaptor.getValue().getClass());
        assertHeaders(httpRequestCaptor, true);
    }

    private PEPRequest createMockAPIRequest() {
        final uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Header testApiHeader =
                new uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Header();
        testApiHeader.setClientReferenceId(UUID.randomUUID().toString());
        final PEPRequest testApiRequest = new PEPRequest();
        testApiRequest.setHeader(testApiHeader);

        return testApiRequest;
    }

    private void assertHeaders(
            ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor,
            boolean crosscoreV2Enabled) {
        // Check Headers
        Map<String, String> httpHeadersKV =
                Arrays.stream(httpRequestCaptor.getValue().getAllHeaders())
                        .collect(Collectors.toMap(Header::getName, Header::getValue));

        assertNotNull(httpHeadersKV.get("Content-Type"));
        assertEquals("application/json", httpHeadersKV.get("Content-Type"));

        if (crosscoreV2Enabled) {
            assertNotNull(httpHeadersKV.get("Accept"));
            assertEquals("application/json", httpHeadersKV.get("Accept"));
            assertNotNull(httpHeadersKV.get("Authorization"));
            assertEquals("Bearer " + TEST_ACCESS_TOKEN, httpHeadersKV.get("Authorization"));
        } else {
            assertEquals(HMAC_OF_REQUEST_BODY, httpHeadersKV.get("hmac-signature"));
        }
    }
}
