package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.PEPRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.PEPResponse;
import uk.gov.di.ipv.cri.fraud.api.util.SleepHelper;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import javax.net.ssl.SSLSession;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_LATENCY_MILLIS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_REQUEST_SEND_OK;

@ExtendWith(MockitoExtension.class)
class ThirdPartyFraudGatewayComponentTest {

    private static final String TEST_ENDPOINT_URL = "https://test-endpoint.co.uk";
    private ThirdPartyFraudGateway thirdPartyFraudGateway;
    @Mock private HttpClient mockHttpClient;
    @Mock private IdentityVerificationRequestMapper mockRequestMapper;
    @Mock private IdentityVerificationResponseMapper mockResponseMapper;
    @Mock private HmacGenerator mockHmacGenerator;
    @Mock private SleepHelper sleepHelper;
    @Mock private EventProbe mockEventProbe;

    @BeforeEach
    void setUp() {
        this.thirdPartyFraudGateway =
                new ThirdPartyFraudGateway(
                        mockHttpClient,
                        mockRequestMapper,
                        mockResponseMapper,
                        new ObjectMapper(),
                        mockHmacGenerator,
                        TEST_ENDPOINT_URL,
                        sleepHelper,
                        mockEventProbe);
    }

    @Test
    void testPepsResponseBodyCanBeDeserialized() throws IOException, InterruptedException {
        final PEPRequest testApiRequest = new PEPRequest();

        final String hmacOfRequestBody = "hmac-of-request-body";
        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        IdentityVerificationResponse testResponse = new IdentityVerificationResponse();
        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        when(mockRequestMapper.mapPEPPersonIdentity(personIdentity)).thenReturn(testApiRequest);

        when(this.mockHmacGenerator.generateHmac(anyString())).thenReturn(hmacOfRequestBody);
        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(this.mockHttpClient.send(
                        httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(
                        createMockApiResponse(
                                200,
                                "{\n"
                                        + "\t\"responseHeader\": {\n"
                                        + "\t\t\"requestType\": \"PepSanctions01\",\n"
                                        + "\t\t\"clientReferenceId\": \"2b58b407-60bc-4356-849c-e6b0ac9dc32a\",\n"
                                        + "\t\t\"expRequestId\": \"123456789\",\n"
                                        + "\t\t\"messageTime\": \"2023-02-22T18:42:59Z\",\n"
                                        + "\t\t\"overallResponse\": {\n"
                                        + "\t\t\t\"decision\": \"CONTINUE\",\n"
                                        + "\t\t\t\"decisionText\": \"Continue\",\n"
                                        + "\t\t\t\"decisionReasons\": [\"No matches present\"],\n"
                                        + "\t\t\t\"recommendedNextActions\": [],\n"
                                        + "\t\t\t\"spareObjects\": []\n"
                                        + "\t\t},\n"
                                        + "\t\t\"responseCode\": \"R0201\",\n"
                                        + "\t\t\"responseType\": \"INFO\",\n"
                                        + "\t\t\"responseMessage\": \"Workflow Complete.\",\n"
                                        + "\t\t\"tenantID\": \"123456\"\n"
                                        + "\t},\n"
                                        + "\t\"clientResponsePayload\": {\n"
                                        + "\t\t\"orchestrationDecisions\": [{\n"
                                        + "\t\t\t\"sequenceId\": \"1\",\n"
                                        + "\t\t\t\"decisionSource\": \"Hunter\",\n"
                                        + "\t\t\t\"decision\": \"NO\",\n"
                                        + "\t\t\t\"decisionReasons\": [\"No relevant Hunter matches present\"],\n"
                                        + "\t\t\t\"score\": 0,\n"
                                        + "\t\t\t\"decisionText\": \"No relevant Matches\",\n"
                                        + "\t\t\t\"nextAction\": \"Continue\",\n"
                                        + "\t\t\t\"decisionTime\": \"2023-02-22T18:43:00Z\"\n"
                                        + "\t\t}],\n"
                                        + "\t\t\"decisionElements\": [{\n"
                                        + "\t\t\t\"serviceName\": \"Peps\",\n"
                                        + "\t\t\t\"score\": 0,\n"
                                        + "\t\t\t\"appReference\": \"2b58b407-60bc-4356-849c-e6b0ac9dc32a\",\n"
                                        + "\t\t\t\"otherData\": {\n"
                                        + "\t\t\t\t\"response\": {\n"
                                        + "\t\t\t\t\t\"matchSummary\": {\n"
                                        + "\t\t\t\t\t\t\"totalMatchScore\": \"0\",\n"
                                        + "\t\t\t\t\t\t\"submissionScores\": {\n"
                                        + "\t\t\t\t\t\t\t\"scoreType\": [{\n"
                                        + "\t\t\t\t\t\t\t\t\"scoreValue\": [{\n"
                                        + "\t\t\t\t\t\t\t\t\t\"value\": 50,\n"
                                        + "\t\t\t\t\t\t\t\t\t\"currentVersion\": \"TRUE\"\n"
                                        + "\t\t\t\t\t\t\t\t}],\n"
                                        + "\t\t\t\t\t\t\t\t\"scoreCount\": 1\n"
                                        + "\t\t\t\t\t\t\t}],\n"
                                        + "\t\t\t\t\t\t\t\"matches\": 1\n"
                                        + "\t\t\t\t\t\t},\n"
                                        + "\t\t\t\t\t\t\"errorWarnings\": {\n"
                                        + "\t\t\t\t\t\t\t\"errors\": {\n"
                                        + "\t\t\t\t\t\t\t\t\"error\": [],\n"
                                        + "\t\t\t\t\t\t\t\t\"errorCount\": 0\n"
                                        + "\t\t\t\t\t\t\t},\n"
                                        + "\t\t\t\t\t\t\t\"warnings\": {\n"
                                        + "\t\t\t\t\t\t\t\t\"warning\": [],\n"
                                        + "\t\t\t\t\t\t\t\t\"warningCount\": 0\n"
                                        + "\t\t\t\t\t\t\t}\n"
                                        + "\t\t\t\t\t\t}\n"
                                        + "\t\t\t\t\t}\n"
                                        + "\t\t\t\t},\n"
                                        + "\t\t\t\t\"scores\": []\n"
                                        + "\n"
                                        + "\t\t\t},\n"
                                        + "\t\t\t\"originalRequestData\": {}\n"
                                        + "\t\t}]\n"
                                        + "\t}\n"
                                        + "}"));

        when(this.mockResponseMapper.mapPEPResponse(any(PEPResponse.class)))
                .thenReturn(testFraudCheckResult);

        FraudCheckResult actualFraudCheckResult =
                thirdPartyFraudGateway.performFraudCheck(personIdentity, true);

        verify(mockEventProbe, times(1)).counterMetric(THIRD_PARTY_REQUEST_CREATED);
        verify(mockEventProbe, times(1)).counterMetric(THIRD_PARTY_REQUEST_SEND_OK);
        verify(mockEventProbe, times(1))
                .counterMetric(eq(THIRD_PARTY_PEP_RESPONSE_LATENCY_MILLIS), anyDouble());

        verify(mockRequestMapper).mapPEPPersonIdentity(personIdentity);
        verify(mockHmacGenerator).generateHmac(anyString());
        verify(mockHttpClient)
                .send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()));
        verify(mockResponseMapper).mapPEPResponse(any(PEPResponse.class));
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

    private HttpResponse<String> createMockApiResponse(int statusCode, String response) {

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
                return response;
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
