package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.api.domain.AddressType;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.IdentityVerificationRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @BeforeEach
    void setUp() {
        this.thirdPartyFraudGateway =
                new ThirdPartyFraudGateway(
                        mockHttpClient,
                        mockRequestMapper,
                        mockResponseMapper,
                        mockObjectMapper,
                        mockHmacGenerator,
                        TEST_ENDPOINT_URL);
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
                .thenReturn(createMockApiResponse());
        when(this.mockObjectMapper.readValue(
                        TEST_API_RESPONSE_BODY, IdentityVerificationResponse.class))
                .thenReturn(testResponse);
        when(this.mockResponseMapper.mapIdentityVerificationResponse(testResponse))
                .thenReturn(testFraudCheckResult);

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(personIdentity);

        verify(mockRequestMapper).mapPersonIdentity(personIdentity);
        verify(mockObjectMapper).writeValueAsString(testApiRequest);
        verify(mockHmacGenerator).generateHmac(testRequestBody);
        verify(mockResponseMapper).mapIdentityVerificationResponse(testResponse);
        verify(mockHttpClient)
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
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

    private HttpResponse<String> createMockApiResponse() {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return 200;
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
}
