package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.fraud.api.service.ServiceFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialHandlerTest {
    @Mock private ServiceFactory mockServiceFactory;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private IdentityVerificationService mockIdentityVerificationService;
    @Mock private EventProbe mockEventProbe;
    @Mock private Context context;
    @Mock private PersonIdentityService personIdentityService;
    @Mock private SessionService sessionService;
    @Mock private DataStore dataStore;
    @Mock private ConfigurationService configurationService;

    private FraudHandler fraudHandler;

    @BeforeEach
    void setup() {
        when(mockServiceFactory.getIdentityVerificationService())
                .thenReturn(mockIdentityVerificationService);
        this.fraudHandler =
                new FraudHandler(
                        mockServiceFactory,
                        mockObjectMapper,
                        mockEventProbe,
                        personIdentityService,
                        sessionService,
                        dataStore,
                        configurationService);
    }

    @Test
    void handleResponseShouldReturnOkResponseWhenValidInputProvided() throws IOException {
        String testRequestBody = "request body";
        PersonIdentity testPersonIdentity = new PersonIdentity();
        IdentityVerificationResult testIdentityVerificationResult =
                new IdentityVerificationResult();
        testIdentityVerificationResult.setSuccess(true);
        testIdentityVerificationResult.setContraIndicators(new String[] {"A01"});
        testIdentityVerificationResult.setIdentityCheckScore(1);
        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        when(mockRequestEvent.getHeaders())
                .thenReturn(Map.of("session_id", UUID.randomUUID().toString()));
        when(mockObjectMapper.readValue(testRequestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);
        when(mockIdentityVerificationService.verifyIdentity(testPersonIdentity))
                .thenReturn(testIdentityVerificationResult);
        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        final SessionItem sessionItem = new SessionItem();
        sessionItem.setSessionId(UUID.randomUUID());
        when(sessionService.getSession(anyString())).thenReturn(sessionItem);

        APIGatewayProxyResponseEvent responseEvent =
                fraudHandler.handleRequest(mockRequestEvent, context);

        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
        assertEquals(
                "{\"success\":true,\"validationErrors\":null,\"error\":null,\"contraIndicators\":[\"A01\"],\"identityCheckScore\":1}",
                responseEvent.getBody());
    }

    @Test
    void handleResponseShouldReturnInternalServerErrorResponseWhenUnableToContactThirdPartyApi()
            throws IOException {
        String testRequestBody = "request body";
        String errorMessage = "error message";
        PersonIdentity testPersonIdentity = new PersonIdentity();
        IdentityVerificationResult testIdentityVerificationResult =
                new IdentityVerificationResult();
        testIdentityVerificationResult.setSuccess(false);
        testIdentityVerificationResult.setError(errorMessage);
        testIdentityVerificationResult.setContraIndicators(new String[] {});
        testIdentityVerificationResult.setIdentityCheckScore(0);
        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);
        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        when(mockObjectMapper.readValue(testRequestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);
        when(mockIdentityVerificationService.verifyIdentity(testPersonIdentity))
                .thenReturn(testIdentityVerificationResult);
        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");

        APIGatewayProxyResponseEvent responseEvent =
                fraudHandler.handleRequest(mockRequestEvent, context);

        assertNotNull(responseEvent);
        assertEquals(500, responseEvent.getStatusCode());
        assertEquals("{\"error_description\":\"error message\"}", responseEvent.getBody());
    }
}
