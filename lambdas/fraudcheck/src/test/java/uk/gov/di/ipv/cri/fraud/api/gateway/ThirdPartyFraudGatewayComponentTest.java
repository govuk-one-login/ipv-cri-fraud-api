package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.check.PepCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.PEPRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.PEPResponse;
import uk.gov.di.ipv.cri.fraud.api.service.CrosscoreV2Configuration;
import uk.gov.di.ipv.cri.fraud.api.service.FraudCheckConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.HttpRetryer;
import uk.gov.di.ipv.cri.fraud.api.service.PepCheckHttpRetryStatusConfig;
import uk.gov.di.ipv.cri.fraud.api.util.HTTPReply;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_LATENCY_MILLIS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_REQUEST_CREATED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_REQUEST_SEND_OK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_RESPONSE_TYPE_EXPECTED_HTTP_STATUS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.ThirdPartyAPIEndpointMetric.PEP_RESPONSE_TYPE_VALID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ThirdPartyFraudGatewayComponentTest {

    private static final String TEST_ENDPOINT_URL = "https://test-endpoint.co.uk";
    private ThirdPartyPepGateway thirdPartyPepGateway;
    @Mock private FraudCheckConfigurationService mockFraudCheckConfigurationService;
    @Mock private CrosscoreV2Configuration mockCrosscoreV2ConfigurationService;
    @Mock private HttpRetryer mockHttpRetryer;
    @Mock private IdentityVerificationRequestMapper mockRequestMapper;
    @Mock private IdentityVerificationResponseMapper mockResponseMapper;
    @Mock private HmacGenerator mockHmacGenerator;
    @Mock private EventProbe mockEventProbe;

    @BeforeEach
    void setUp() {
        this.thirdPartyPepGateway =
                new ThirdPartyPepGateway(
                        mockHttpRetryer,
                        mockRequestMapper,
                        mockResponseMapper,
                        new ObjectMapper(),
                        mockFraudCheckConfigurationService,
                        mockEventProbe);
    }

    @Test
    void testCrosscoreV2PepsResponseBodyCanBeDeserialized()
            throws IOException, OAuthErrorResponseException {
        final PEPRequest testApiRequest = new PEPRequest();

        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentity(AddressType.CURRENT);
        PepCheckResult testPepCheckResult = new PepCheckResult();
        when(mockFraudCheckConfigurationService.getCrosscoreV2Configuration())
                .thenReturn(mockCrosscoreV2ConfigurationService);
        when(mockCrosscoreV2ConfigurationService.getTenantId()).thenReturn("123456");
        when(mockRequestMapper.mapPEPPersonIdentity(personIdentity, "123456"))
                .thenReturn(testApiRequest);

        ArgumentCaptor<HttpEntityEnclosingRequestBase> httpRequestCaptor =
                ArgumentCaptor.forClass(HttpPost.class);

        // EMTODO, verify response format is the V2 in post go live cleanup (see stub response)
        String responseBody =
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
                        + "}";

        HTTPReply httpReply = new HTTPReply(200, null, responseBody);

        when(mockHttpRetryer.sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(PepCheckHttpRetryStatusConfig.class),
                        eq("Pep Check")))
                .thenReturn(httpReply);

        when(this.mockResponseMapper.mapPEPResponse(any(PEPResponse.class)))
                .thenReturn(testPepCheckResult);

        PepCheckResult actualPepCheckResult =
                thirdPartyPepGateway.performPepCheck(personIdentity, "testAccessTokenValue");

        InOrder inOrderMockEventProbe = inOrder(mockEventProbe);
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_REQUEST_CREATED.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_REQUEST_SEND_OK.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(eq(THIRD_PARTY_PEP_RESPONSE_LATENCY_MILLIS), anyDouble());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_RESPONSE_TYPE_EXPECTED_HTTP_STATUS.withEndpointPrefix());
        inOrderMockEventProbe
                .verify(mockEventProbe)
                .counterMetric(PEP_RESPONSE_TYPE_VALID.withEndpointPrefix());
        verifyNoMoreInteractions(mockEventProbe);

        verify(mockRequestMapper).mapPEPPersonIdentity(personIdentity, "123456");
        verify(mockHttpRetryer)
                .sendHTTPRequestRetryIfAllowed(
                        httpRequestCaptor.capture(),
                        any(PepCheckHttpRetryStatusConfig.class),
                        eq("Pep Check"));
        verify(mockResponseMapper).mapPEPResponse(any(PEPResponse.class));

        assertNotNull(actualPepCheckResult);
        assertEquals(HttpPost.class, httpRequestCaptor.getValue().getClass());

        // Check Headers
        Map<String, String> httpHeadersKV =
                Arrays.stream(httpRequestCaptor.getValue().getAllHeaders())
                        .collect(Collectors.toMap(Header::getName, Header::getValue));

        assertNotNull(httpHeadersKV.get("Content-Type"));
        assertNotNull(httpHeadersKV.get("Accept"));
        assertEquals("application/json", httpHeadersKV.get("Accept"));
        assertNotNull(httpHeadersKV.get("Authorization"));
        assertEquals("Bearer testAccessTokenValue", httpHeadersKV.get("Authorization"));
    }
}
