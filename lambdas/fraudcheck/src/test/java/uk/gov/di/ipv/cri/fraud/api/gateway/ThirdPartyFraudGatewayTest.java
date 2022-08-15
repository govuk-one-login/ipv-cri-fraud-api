package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.IdentityVerificationRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.util.SleepHelper;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import javax.net.ssl.SSLSession;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThirdPartyFraudGatewayTest {

    private static class ExperianGatewayConstructorArgs {
        private final HttpClient httpClient;
        private final IdentityVerificationRequestMapper requestMapper;
        private final IdentityVerificationResponseMapper responseMapper;
        private final ObjectMapper objectMapper;
        private final HmacGenerator hmacGenerator;
        private final String experianEndpointUrl;

        private ExperianGatewayConstructorArgs(
                HttpClient httpClient,
                IdentityVerificationRequestMapper requestMapper,
                IdentityVerificationResponseMapper responseMapper,
                ObjectMapper objectMapper,
                HmacGenerator hmacGenerator,
                String experianEndpointUrl) {

            this.httpClient = httpClient;
            this.requestMapper = requestMapper;
            this.responseMapper = responseMapper;
            this.objectMapper = objectMapper;
            this.hmacGenerator = hmacGenerator;
            this.experianEndpointUrl = experianEndpointUrl;
        }
    }

    private static final String TEST_API_RESPONSE_BODY = "test-api-response-content";
    private static final String TEST_ENDPOINT_URL = "https://test-endpoint.co.uk";
    private ThirdPartyFraudGateway thirdPartyFraudGateway;
    @Mock private HttpClient mockHttpClient;
    @Mock private IdentityVerificationRequestMapper mockRequestMapper;
    @Mock private IdentityVerificationResponseMapper mockResponseMapper;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private HmacGenerator mockHmacGenerator;
    @Mock private SleepHelper sleepHelper;

    @BeforeEach
    void setUp() {
        this.thirdPartyFraudGateway =
                new ThirdPartyFraudGateway(
                        mockHttpClient,
                        mockRequestMapper,
                        mockResponseMapper,
                        mockObjectMapper,
                        mockHmacGenerator,
                        TEST_ENDPOINT_URL,
                        sleepHelper);
    }

    @Test
    void shouldInvokeExperianApi() throws IOException, InterruptedException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final IdentityVerificationRequest testApiRequest = new IdentityVerificationRequest();

        final String hmacOfRequestBody = "hmac-of-request-body";
        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        IdentityVerificationResponse testResponse = new IdentityVerificationResponse();
        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        when(mockRequestMapper.mapPersonIdentity(personIdentity)).thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        when(this.mockHmacGenerator.generateHmac(testRequestBody)).thenReturn(hmacOfRequestBody);
        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(this.mockHttpClient.send(
                        httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(createMockApiResponse(200));
        when(this.mockObjectMapper.readValue(
                        TEST_API_RESPONSE_BODY, IdentityVerificationResponse.class))
                .thenReturn(testResponse);
        when(this.mockResponseMapper.mapIdentityVerificationResponse(testResponse))
                .thenReturn(testFraudCheckResult);

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(personIdentity, false);

        verify(mockRequestMapper).mapPersonIdentity(personIdentity);
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHmacGenerator).generateHmac(testRequestBody);
        verify(mockHttpClient)
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
        verify(mockResponseMapper).mapIdentityVerificationResponse(testResponse);
        assertNotNull(actualFraudCheckResult);
        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/json", capturedHttpRequestHeaders.firstValue("Content-Type").get());
        assertEquals(
                hmacOfRequestBody, capturedHttpRequestHeaders.firstValue("hmac-signature").get());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP300Response() throws IOException, InterruptedException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final IdentityVerificationRequest testApiRequest = new IdentityVerificationRequest();

        final String hmacOfRequestBody = "hmac-of-request-body";
        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        when(mockRequestMapper.mapPersonIdentity(personIdentity)).thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        when(this.mockHmacGenerator.generateHmac(testRequestBody)).thenReturn(hmacOfRequestBody);
        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

        final int MOCK_HTTP_STATUS_CODE = 300;

        when(this.mockHttpClient.send(
                        httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE));

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(personIdentity, false);

        final String EXPECTED_ERROR =
                ThirdPartyFraudGateway.HTTP_300_REDIRECT_MESSAGE + MOCK_HTTP_STATUS_CODE;

        verify(mockRequestMapper).mapPersonIdentity(personIdentity);
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHmacGenerator).generateHmac(testRequestBody);

        verify(mockHttpClient, times(1))
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
        assertNotNull(actualFraudCheckResult);
        assertEquals(EXPECTED_ERROR, actualFraudCheckResult.getErrorMessage());

        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/json", capturedHttpRequestHeaders.firstValue("Content-Type").get());
        assertEquals(
                hmacOfRequestBody, capturedHttpRequestHeaders.firstValue("hmac-signature").get());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP400Response() throws IOException, InterruptedException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final IdentityVerificationRequest testApiRequest = new IdentityVerificationRequest();

        final String hmacOfRequestBody = "hmac-of-request-body";
        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        when(mockRequestMapper.mapPersonIdentity(personIdentity)).thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        when(this.mockHmacGenerator.generateHmac(testRequestBody)).thenReturn(hmacOfRequestBody);
        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

        final int MOCK_HTTP_STATUS_CODE = 400;

        when(this.mockHttpClient.send(
                        httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE));

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(personIdentity, false);

        final String EXPECTED_ERROR =
                ThirdPartyFraudGateway.HTTP_400_CLIENT_REQUEST_ERROR + MOCK_HTTP_STATUS_CODE;

        verify(mockRequestMapper).mapPersonIdentity(personIdentity);
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHmacGenerator).generateHmac(testRequestBody);

        verify(mockHttpClient, times(1))
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
        assertNotNull(actualFraudCheckResult);
        assertEquals(EXPECTED_ERROR, actualFraudCheckResult.getErrorMessage());

        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/json", capturedHttpRequestHeaders.firstValue("Content-Type").get());
        assertEquals(
                hmacOfRequestBody, capturedHttpRequestHeaders.firstValue("hmac-signature").get());
    }

    @Test
    void thirdPartyApiReturnsErrorOnHTTP500Response() throws IOException, InterruptedException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final IdentityVerificationRequest testApiRequest = new IdentityVerificationRequest();

        final String hmacOfRequestBody = "hmac-of-request-body";
        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        when(mockRequestMapper.mapPersonIdentity(personIdentity)).thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        when(this.mockHmacGenerator.generateHmac(testRequestBody)).thenReturn(hmacOfRequestBody);
        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

        final int MOCK_HTTP_STATUS_CODE = 500;

        when(this.mockHttpClient.send(
                        httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE));

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(personIdentity, false);

        final String EXPECTED_ERROR =
                ThirdPartyFraudGateway.HTTP_500_SERVER_ERROR + MOCK_HTTP_STATUS_CODE;

        verify(mockRequestMapper).mapPersonIdentity(personIdentity);
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHmacGenerator).generateHmac(testRequestBody);

        // +1 for Initial send
        verify(mockHttpClient, times(ThirdPartyFraudGateway.MAX_HTTP_RETRIES + 1))
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
        assertNotNull(actualFraudCheckResult);
        assertEquals(EXPECTED_ERROR, actualFraudCheckResult.getErrorMessage());

        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/json", capturedHttpRequestHeaders.firstValue("Content-Type").get());
        assertEquals(
                hmacOfRequestBody, capturedHttpRequestHeaders.firstValue("hmac-signature").get());
    }

    @Test
    void thirdPartyApiReturnsErrorOnUnhandledHTTPResponse()
            throws IOException, InterruptedException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final IdentityVerificationRequest testApiRequest = new IdentityVerificationRequest();

        final String hmacOfRequestBody = "hmac-of-request-body";
        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        when(mockRequestMapper.mapPersonIdentity(personIdentity)).thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        when(this.mockHmacGenerator.generateHmac(testRequestBody)).thenReturn(hmacOfRequestBody);
        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

        final int MOCK_HTTP_STATUS_CODE = -1;

        when(this.mockHttpClient.send(
                        httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE));

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(personIdentity, false);

        final String EXPECTED_ERROR =
                ThirdPartyFraudGateway.HTTP_UNHANDLED_ERROR + MOCK_HTTP_STATUS_CODE;

        verify(mockRequestMapper).mapPersonIdentity(personIdentity);
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHmacGenerator).generateHmac(testRequestBody);

        verify(mockHttpClient, times(1))
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
        assertNotNull(actualFraudCheckResult);
        assertEquals(EXPECTED_ERROR, actualFraudCheckResult.getErrorMessage());

        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/json", capturedHttpRequestHeaders.firstValue("Content-Type").get());
        assertEquals(
                hmacOfRequestBody, capturedHttpRequestHeaders.firstValue("hmac-signature").get());
    }

    @ParameterizedTest
    @MethodSource("getRetryStatusCodes") // Retry status codes
    void retryThirdPartyApiHTTPResponseForStatusCode(int initialStatusCodeResponse)
            throws IOException, InterruptedException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final IdentityVerificationRequest testApiRequest = new IdentityVerificationRequest();

        final String hmacOfRequestBody = "hmac-of-request-body";
        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        IdentityVerificationResponse testResponse = new IdentityVerificationResponse();
        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        when(mockRequestMapper.mapPersonIdentity(personIdentity)).thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        when(this.mockHmacGenerator.generateHmac(testRequestBody)).thenReturn(hmacOfRequestBody);
        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(this.mockHttpClient.send(
                        httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(createMockApiResponse(initialStatusCodeResponse))
                .thenReturn(createMockApiResponse(200));
        when(this.mockObjectMapper.readValue(
                        TEST_API_RESPONSE_BODY, IdentityVerificationResponse.class))
                .thenReturn(testResponse);
        when(this.mockResponseMapper.mapIdentityVerificationResponse(testResponse))
                .thenReturn(testFraudCheckResult);

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(personIdentity, false);

        verify(mockRequestMapper).mapPersonIdentity(personIdentity);
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHmacGenerator).generateHmac(testRequestBody);
        verify(mockHttpClient, times(2))
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
        verify(mockResponseMapper).mapIdentityVerificationResponse(testResponse);
        assertNotNull(actualFraudCheckResult);
        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/json", capturedHttpRequestHeaders.firstValue("Content-Type").get());
        assertEquals(
                hmacOfRequestBody, capturedHttpRequestHeaders.firstValue("hmac-signature").get());
    }

    @Test
    void retryThirdPartyApiUpNTimesAndPass() throws IOException, InterruptedException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final IdentityVerificationRequest testApiRequest = new IdentityVerificationRequest();

        final String hmacOfRequestBody = "hmac-of-request-body";
        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        IdentityVerificationResponse testResponse = new IdentityVerificationResponse();
        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        when(mockRequestMapper.mapPersonIdentity(personIdentity)).thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        when(this.mockHmacGenerator.generateHmac(testRequestBody)).thenReturn(hmacOfRequestBody);
        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(this.mockHttpClient.send(
                        httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(createMockApiResponse(501)) // Initial
                .thenReturn(createMockApiResponse(501)) // Retry 1
                .thenReturn(createMockApiResponse(501))
                .thenReturn(createMockApiResponse(501))
                .thenReturn(createMockApiResponse(501))
                .thenReturn(createMockApiResponse(501))
                .thenReturn(createMockApiResponse(501))
                .thenReturn(createMockApiResponse(200)); // Retry 7 Ok
        when(this.mockObjectMapper.readValue(
                        TEST_API_RESPONSE_BODY, IdentityVerificationResponse.class))
                .thenReturn(testResponse);
        when(this.mockResponseMapper.mapIdentityVerificationResponse(testResponse))
                .thenReturn(testFraudCheckResult);

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(personIdentity, false);

        verify(mockRequestMapper).mapPersonIdentity(personIdentity);
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHmacGenerator).generateHmac(testRequestBody);
        verify(mockHttpClient, times(ThirdPartyFraudGateway.MAX_HTTP_RETRIES + 1))
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
        verify(mockResponseMapper).mapIdentityVerificationResponse(testResponse);
        assertNotNull(actualFraudCheckResult);
        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/json", capturedHttpRequestHeaders.firstValue("Content-Type").get());
        assertEquals(
                hmacOfRequestBody, capturedHttpRequestHeaders.firstValue("hmac-signature").get());
    }

    @Test
    void retryThirdPartyApiUpNTimesAndFail() throws IOException, InterruptedException {
        final String testRequestBody = "serialisedCrossCoreApiRequest";
        final IdentityVerificationRequest testApiRequest = new IdentityVerificationRequest();

        final String hmacOfRequestBody = "hmac-of-request-body";
        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        IdentityVerificationResponse testResponse = new IdentityVerificationResponse();
        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        when(mockRequestMapper.mapPersonIdentity(personIdentity)).thenReturn(testApiRequest);

        when(this.mockObjectMapper.writeValueAsString(testApiRequest)).thenReturn(testRequestBody);
        when(this.mockHmacGenerator.generateHmac(testRequestBody)).thenReturn(hmacOfRequestBody);
        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

        final int MOCK_HTTP_STATUS_CODE = 501;

        when(this.mockHttpClient.send(
                        httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE)) // Initial
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE)) // Retry 1
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE))
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE))
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE))
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE))
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE))
                .thenReturn(createMockApiResponse(MOCK_HTTP_STATUS_CODE)); // Retry 7 Fail

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(personIdentity, false);

        final String EXPECTED_ERROR =
                ThirdPartyFraudGateway.HTTP_500_SERVER_ERROR + MOCK_HTTP_STATUS_CODE;

        verify(mockRequestMapper).mapPersonIdentity(personIdentity);
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHmacGenerator).generateHmac(testRequestBody);

        verify(mockHttpClient, times(ThirdPartyFraudGateway.MAX_HTTP_RETRIES + 1))
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
        assertNotNull(actualFraudCheckResult);
        assertEquals(EXPECTED_ERROR, actualFraudCheckResult.getErrorMessage());

        assertEquals(TEST_ENDPOINT_URL, httpRequestCaptor.getValue().uri().toString());
        assertEquals("POST", httpRequestCaptor.getValue().method());
        HttpHeaders capturedHttpRequestHeaders = httpRequestCaptor.getValue().headers();
        assertEquals("application/json", capturedHttpRequestHeaders.firstValue("Accept").get());
        assertEquals(
                "application/json", capturedHttpRequestHeaders.firstValue("Content-Type").get());
        assertEquals(
                hmacOfRequestBody, capturedHttpRequestHeaders.firstValue("hmac-signature").get());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenInvalidConstructorArgumentsProvided() {
        Map<String, ExperianGatewayConstructorArgs> testCases =
                Map.of(
                        "httpClient must not be null",
                        new ExperianGatewayConstructorArgs(null, null, null, null, null, null),
                        "requestMapper must not be null",
                        new ExperianGatewayConstructorArgs(
                                Mockito.mock(HttpClient.class), null, null, null, null, null),
                        "responseMapper must not be null",
                        new ExperianGatewayConstructorArgs(
                                Mockito.mock(HttpClient.class),
                                Mockito.mock(IdentityVerificationRequestMapper.class),
                                null,
                                null,
                                null,
                                null),
                        "objectMapper must not be null",
                        new ExperianGatewayConstructorArgs(
                                Mockito.mock(HttpClient.class),
                                Mockito.mock(IdentityVerificationRequestMapper.class),
                                Mockito.mock(IdentityVerificationResponseMapper.class),
                                null,
                                null,
                                null),
                        "hmacGenerator must not be null",
                        new ExperianGatewayConstructorArgs(
                                Mockito.mock(HttpClient.class),
                                Mockito.mock(IdentityVerificationRequestMapper.class),
                                Mockito.mock(IdentityVerificationResponseMapper.class),
                                Mockito.mock(ObjectMapper.class),
                                null,
                                null),
                        "crossCoreApiConfig must not be null",
                        new ExperianGatewayConstructorArgs(
                                Mockito.mock(HttpClient.class),
                                Mockito.mock(IdentityVerificationRequestMapper.class),
                                Mockito.mock(IdentityVerificationResponseMapper.class),
                                Mockito.mock(ObjectMapper.class),
                                Mockito.mock(HmacGenerator.class),
                                null));

        testCases.forEach(
                (errorMessage, constructorArgs) ->
                        assertThrows(
                                NullPointerException.class,
                                () ->
                                        new ThirdPartyFraudGateway(
                                                constructorArgs.httpClient,
                                                constructorArgs.requestMapper,
                                                constructorArgs.responseMapper,
                                                constructorArgs.objectMapper,
                                                constructorArgs.hmacGenerator,
                                                constructorArgs.experianEndpointUrl),
                                errorMessage));
    }

    private HttpResponse<String> createMockApiResponse(int statusCode) {

        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return statusCode;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public String body() {
                return TEST_API_RESPONSE_BODY;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }

    private static Stream<Integer> getRetryStatusCodes() {
        Stream<Integer> retryStatusCodes = Stream.of(429);
        Stream<Integer> serverErrorRetryStatusCodes = IntStream.range(500, 599).boxed();
        return Stream.concat(retryStatusCodes, serverErrorRetryStatusCodes);
    }
}
