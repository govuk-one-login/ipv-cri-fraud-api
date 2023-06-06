package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
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
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.fraud.api.service.ServiceFactory;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.LAMBDA_IDENTITY_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.LAMBDA_IDENTITY_CHECK_COMPLETED_OK;

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
    @Mock private AuditService auditService;
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
                        configurationService,
                        auditService);
    }

    @Test
    void handleResponseShouldReturnOkResponseWhenValidInputProvided()
            throws IOException, SqsException {
        String testRequestBody = "request body";
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        IdentityVerificationResult testIdentityVerificationResult =
                new IdentityVerificationResult();
        testIdentityVerificationResult.setSuccess(true);
        testIdentityVerificationResult.setContraIndicators(List.of("A01"));
        testIdentityVerificationResult.setIdentityCheckScore(1);
        testIdentityVerificationResult.setDecisionScore("90");
        testIdentityVerificationResult.setChecksSucceeded(
                List.of("check_one", "check_two", "check_three"));
        testIdentityVerificationResult.setChecksFailed(new ArrayList<>());

        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        UUID sessionId = UUID.randomUUID();
        Map<String, String> requestHeaders = Map.of("session_id", sessionId.toString());

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);

        final var sessionItem = new SessionItem();
        sessionItem.setSessionId(sessionId);
        when(sessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(mockObjectMapper.readValue(testRequestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);

        doNothing()
                .when(auditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));

        when(mockIdentityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders))
                .thenReturn(testIdentityVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                fraudHandler.handleRequest(mockRequestEvent, context);

        verify(mockEventProbe).counterMetric(LAMBDA_IDENTITY_CHECK_COMPLETED_OK);
        final FraudResultItem fraudResultItem =
                populateFraudResultItem(testIdentityVerificationResult, sessionItem);

        verify(dataStore).create(fraudResultItem);

        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
        assertEquals(
                "{\"success\":true,\"validationErrors\":null,\"error\":null,\"contraIndicators\":[\"A01\"],\"identityCheckScore\":1,\"activityHistoryScore\":0,\"activityFrom\":null,\"transactionId\":null,\"pepTransactionId\":null,\"decisionScore\":\"90\",\"thirdPartyFraudCodes\":[],\"checksSucceeded\":[\"check_one\",\"check_two\",\"check_three\"],\"checksFailed\":[]}",
                responseEvent.getBody());
    }

    @Test
    void handleResponseShouldReturnInternalServerErrorResponseWhenUnableToContactThirdPartyApi()
            throws JsonProcessingException, SqsException {
        String testRequestBody = "request body";
        String errorMessage = "error message";
        PersonIdentity testPersonIdentity = new PersonIdentity();
        IdentityVerificationResult testIdentityVerificationResult =
                new IdentityVerificationResult();
        testIdentityVerificationResult.setSuccess(false);
        testIdentityVerificationResult.setError(errorMessage);
        testIdentityVerificationResult.setContraIndicators(List.of(""));
        testIdentityVerificationResult.setIdentityCheckScore(0);

        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        Map<String, String> requestHeaders = Map.of("session_id", UUID.randomUUID().toString());
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);

        final var sessionItem = new SessionItem();
        sessionItem.setSessionId(UUID.randomUUID());
        when(sessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(mockObjectMapper.readValue(testRequestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);

        when(mockIdentityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders))
                .thenReturn(testIdentityVerificationResult);

        doNothing()
                .when(auditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                fraudHandler.handleRequest(mockRequestEvent, context);

        verify(mockEventProbe).counterMetric(LAMBDA_IDENTITY_CHECK_COMPLETED_ERROR);

        assertNotNull(responseEvent);
        assertEquals(500, responseEvent.getStatusCode());
        final String EXPECTED_ERROR = "{\"error_description\":\"error message\"}";
        assertEquals(EXPECTED_ERROR, responseEvent.getBody());
    }

    private FraudResultItem populateFraudResultItem(
            IdentityVerificationResult testIdentityVerificationResult, SessionItem sessionItem) {
        final FraudResultItem fraudResultItem =
                new FraudResultItem(
                        sessionItem.getSessionId(),
                        testIdentityVerificationResult.getContraIndicators(),
                        testIdentityVerificationResult.getIdentityCheckScore(),
                        testIdentityVerificationResult.getActivityHistoryScore(),
                        testIdentityVerificationResult.getDecisionScore());
        fraudResultItem.setTransactionId(testIdentityVerificationResult.getTransactionId());
        fraudResultItem.setPepTransactionId(testIdentityVerificationResult.getPepTransactionId());

        fraudResultItem.setCheckDetails(testIdentityVerificationResult.getChecksSucceeded());
        fraudResultItem.setFailedCheckDetails(testIdentityVerificationResult.getChecksFailed());

        fraudResultItem.setActivityFrom(testIdentityVerificationResult.getActivityFrom());
        return fraudResultItem;
    }
}
