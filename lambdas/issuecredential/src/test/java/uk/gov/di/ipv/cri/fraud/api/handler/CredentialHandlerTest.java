package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.fraud.api.service.ServiceFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialHandlerTest {
    @Mock private ServiceFactory mockServiceFactory;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private IdentityVerificationService mockIdentityVerificationService;
    @Mock private EventProbe mockEventProbe;
    private CredentialHandler credentialHandler;

    @BeforeEach
    void setup() {
        when(mockServiceFactory.getIdentityVerificationService())
                .thenReturn(mockIdentityVerificationService);
        this.credentialHandler =
                new CredentialHandler(mockServiceFactory, mockObjectMapper, mockEventProbe);
    }

    @Test
    void handleResponseShouldReturnOkResponseWhenValidInputProvided()
            throws JsonProcessingException {
        String testRequestBody = "request body";
        PersonIdentity testPersonIdentity = new PersonIdentity();
        IdentityVerificationResult testIdentityVerificationResult =
                new IdentityVerificationResult();
        testIdentityVerificationResult.setSuccess(true);
        testIdentityVerificationResult.setContraIndicators(new String[] {"A01"});
        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        when(mockObjectMapper.readValue(testRequestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);
        when(mockIdentityVerificationService.verifyIdentity(testPersonIdentity))
                .thenReturn(testIdentityVerificationResult);

        APIGatewayProxyResponseEvent responseEvent =
                credentialHandler.handleRequest(mockRequestEvent, null);

        assertNotNull(responseEvent);
        assertEquals(HttpStatusCode.OK, responseEvent.getStatusCode());
        assertEquals(
                "{\"success\":true,\"validationErrors\":null,\"error\":null,\"contraIndicators\":[\"A01\"]}",
                responseEvent.getBody());
    }

    @Test
    void handleResponseShouldReturnInternalServerErrorResponseWhenUnableToContactThirdPartyApi()
            throws JsonProcessingException {
        String testRequestBody = "request body";
        String errorMessage = "error message";
        PersonIdentity testPersonIdentity = new PersonIdentity();
        IdentityVerificationResult testIdentityVerificationResult =
                new IdentityVerificationResult();
        testIdentityVerificationResult.setSuccess(false);
        testIdentityVerificationResult.setError(errorMessage);
        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);
        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        when(mockObjectMapper.readValue(testRequestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);
        when(mockIdentityVerificationService.verifyIdentity(testPersonIdentity))
                .thenReturn(testIdentityVerificationResult);

        APIGatewayProxyResponseEvent responseEvent =
                credentialHandler.handleRequest(mockRequestEvent, null);

        assertNotNull(responseEvent);
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR, responseEvent.getStatusCode());
        assertEquals("{\"error_description\":\"error message\"}", responseEvent.getBody());
    }
}
