package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;
import uk.gov.di.ipv.cri.fraud.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.fraud.library.error.CommonExpressOAuthError;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;
import uk.gov.di.ipv.cri.fraud.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.fraud.library.service.ResultItemStorageService;
import uk.gov.di.ipv.cri.fraud.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.common.library.error.ErrorResponse.SESSION_NOT_FOUND;
import static uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse.ERROR_SENDING_FRAUD_CHECK_REQUEST;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.LAMBDA_FRAUD_CHECK_FUNCTION_INIT_DURATION;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.LAMBDA_IDENTITY_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.LAMBDA_IDENTITY_CHECK_COMPLETED_OK;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class FraudHandlerTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock private ServiceFactory mockServiceFactory;
    @Mock private IdentityVerificationService mockIdentityVerificationService;

    @Mock private EventProbe mockEventProbe;

    @Mock private SessionService mockSessionService;
    @Mock private AuditService mockAuditService;

    @Mock private PersonIdentityService mockPersonIdentityService;

    @Mock private ResultItemStorageService<FraudResultItem> mockResultItemStorageService;

    @Mock private ParameterStoreService mockParameterStoreService;

    @Mock private Context context;
    private FraudHandler fraudHandler;

    @BeforeEach
    void setup() {
        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("AWS_STACK_NAME", "TEST_STACK");
        // EnvVar feature toggles

        when(mockServiceFactory.getParameterStoreService()).thenReturn(mockParameterStoreService);

        when(mockServiceFactory.getEventProbe()).thenReturn(mockEventProbe);
        when(mockServiceFactory.getSessionService()).thenReturn(mockSessionService);
        when(mockServiceFactory.getAuditService()).thenReturn(mockAuditService);

        when(mockServiceFactory.getPersonIdentityService()).thenReturn(mockPersonIdentityService);

        when(mockServiceFactory.getResultItemStorageService())
                .thenReturn(mockResultItemStorageService);
        when(mockParameterStoreService.getParameterValue(
                        ParameterPrefix.COMMON_API,
                        ParameterStoreParameters.FRAUD_RESULT_ITEM_TTL_PARAMETER))
                .thenReturn(String.valueOf(1000L));

        this.fraudHandler = new FraudHandler(mockServiceFactory, mockIdentityVerificationService);
    }

    @Test
    void handleResponseShouldReturnOkResponseWhenValidInputProvided()
            throws IOException, SqsException, OAuthErrorResponseException {
        String testRequestBody = "request body";
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        IdentityVerificationResult testIdentityVerificationResult =
                new IdentityVerificationResult();
        testIdentityVerificationResult.setSuccess(true);
        testIdentityVerificationResult.setContraIndicators(List.of("A01"));
        testIdentityVerificationResult.setIdentityCheckScore(1);
        testIdentityVerificationResult.setDecisionScore(90);
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
        when(mockSessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(mockPersonIdentityService.getPersonIdentity(sessionItem.getSessionId()))
                .thenReturn(testPersonIdentity);

        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));

        when(mockIdentityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders))
                .thenReturn(testIdentityVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                fraudHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_FRAUD_CHECK_FUNCTION_INIT_DURATION), anyDouble());
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_IDENTITY_CHECK_COMPLETED_OK);
        verifyNoMoreInteractions(mockEventProbe);

        final FraudResultItem fraudResultItem =
                populateFraudResultItem(testIdentityVerificationResult, sessionItem);

        verify(mockResultItemStorageService).saveResultItem(fraudResultItem);

        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
    }

    @Test
    void handleResponseShouldReturnOkResponseWhenSessionAttemptGreaterThanOneAndResultFound() {
        String testRequestBody = "request body";

        IdentityVerificationResult testIdentityVerificationResult =
                new IdentityVerificationResult();
        testIdentityVerificationResult.setSuccess(true);
        testIdentityVerificationResult.setContraIndicators(List.of("A01"));
        testIdentityVerificationResult.setIdentityCheckScore(1);
        testIdentityVerificationResult.setDecisionScore(90);
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
        sessionItem.setAttemptCount(1);
        when(mockSessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        final FraudResultItem fraudResultItem =
                populateFraudResultItem(testIdentityVerificationResult, sessionItem);
        when(mockResultItemStorageService.getResultItem(sessionItem.getSessionId()))
                .thenReturn(fraudResultItem);

        // No mapping of person identity
        verifyNoInteractions(mockPersonIdentityService);

        // No audit events for send/received
        verifyNoInteractions(mockAuditService);

        // No Check Down
        verifyNoInteractions(mockIdentityVerificationService);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                fraudHandler.handleRequest(mockRequestEvent, context);

        verify(mockEventProbe).counterMetric(LAMBDA_IDENTITY_CHECK_COMPLETED_OK);

        // No saving of existing result
        verifyNoMoreInteractions(mockResultItemStorageService);

        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
    }

    @Test
    void handleResponseShouldReturnOkResponseWhenSessionAttemptGreaterThanOneAndResultNotFound()
            throws IOException, SqsException, OAuthErrorResponseException {
        String testRequestBody = "request body";
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        IdentityVerificationResult testIdentityVerificationResult =
                new IdentityVerificationResult();
        testIdentityVerificationResult.setSuccess(true);
        testIdentityVerificationResult.setContraIndicators(List.of("A01"));
        testIdentityVerificationResult.setIdentityCheckScore(1);
        testIdentityVerificationResult.setDecisionScore(90);
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
        sessionItem.setAttemptCount(1);
        when(mockSessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(mockPersonIdentityService.getPersonIdentity(sessionItem.getSessionId()))
                .thenReturn(testPersonIdentity);

        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.REQUEST_SENT), any(AuditEventContext.class));

        when(mockIdentityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders))
                .thenReturn(testIdentityVerificationResult);

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                fraudHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_FRAUD_CHECK_FUNCTION_INIT_DURATION), anyDouble());
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_IDENTITY_CHECK_COMPLETED_OK);
        verifyNoMoreInteractions(mockEventProbe);

        final FraudResultItem fraudResultItem =
                populateFraudResultItem(testIdentityVerificationResult, sessionItem);

        verify(mockResultItemStorageService).saveResultItem(fraudResultItem);

        assertNotNull(responseEvent);
        assertEquals(200, responseEvent.getStatusCode());
    }

    @Test
    void handleResponseShouldReturnInternalServerErrorResponseWhenUnableToContactThirdPartyApi()
            throws JsonProcessingException, SqsException, OAuthErrorResponseException {
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
        when(mockSessionService.validateSessionId(anyString())).thenReturn(sessionItem);

        when(mockPersonIdentityService.getPersonIdentity(sessionItem.getSessionId()))
                .thenReturn(testPersonIdentity);

        // Trigger the mapping failure via the mock
        when(mockIdentityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders))
                .thenThrow(
                        new OAuthErrorResponseException(
                                HttpStatusCode.INTERNAL_SERVER_ERROR,
                                ERROR_SENDING_FRAUD_CHECK_REQUEST));

        when(context.getFunctionName()).thenReturn("functionName");
        when(context.getFunctionVersion()).thenReturn("1.0");
        APIGatewayProxyResponseEvent responseEvent =
                fraudHandler.handleRequest(mockRequestEvent, context);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe)
                .counterMetric(eq(LAMBDA_FRAUD_CHECK_FUNCTION_INIT_DURATION), anyDouble());
        inOrder.verify(mockEventProbe).counterMetric(LAMBDA_IDENTITY_CHECK_COMPLETED_ERROR);
        verifyNoMoreInteractions(mockEventProbe);

        assertNotNull(responseEvent);
        assertEquals(500, responseEvent.getStatusCode());
        final String EXPECTED_ERROR =
                "{\"oauth_error\":{\"error_description\":\"Unexpected server error\",\"error\":\"server_error\"}}";
        assertEquals(EXPECTED_ERROR, responseEvent.getBody());
    }

    @Test
    void handleResponseShouldThrowExceptionWhenSessionIdMissing() throws JsonProcessingException {
        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        Map<String, String> headers = new HashMap<>();

        when(mockRequestEvent.getHeaders()).thenReturn(headers);

        APIGatewayProxyResponseEvent responseEvent =
                fraudHandler.handleRequest(mockRequestEvent, context);

        JsonNode responseTreeRootNode = new ObjectMapper().readTree(responseEvent.getBody());
        JsonNode oauthErrorNode = responseTreeRootNode.get("oauth_error");

        CommonExpressOAuthError expectedObject =
                new CommonExpressOAuthError(
                        OAuth2Error.ACCESS_DENIED, SESSION_NOT_FOUND.getMessage());

        assertNotNull(responseEvent);
        assertNotNull(responseTreeRootNode);
        assertNotNull(oauthErrorNode);
        assertEquals(HttpStatusCode.FORBIDDEN, responseEvent.getStatusCode());

        assertEquals(
                "oauth_error",
                responseTreeRootNode.fieldNames().next().toString()); // Root Node Name
        assertEquals(
                expectedObject.getError().get("error"),
                oauthErrorNode.get("error").textValue()); // error
        assertEquals(
                expectedObject.getError().get("error_description"),
                oauthErrorNode.get("error_description").textValue()); // error description
    }

    @Test
    void handleResponseShouldThrowExceptionWhenSessionIdIsInvalid() throws JsonProcessingException {
        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);

        Map<String, String> headers = new HashMap<>();
        headers.put("session_id", "invalid");

        when(mockRequestEvent.getHeaders()).thenReturn(headers);

        APIGatewayProxyResponseEvent responseEvent =
                fraudHandler.handleRequest(mockRequestEvent, context);

        JsonNode responseTreeRootNode = new ObjectMapper().readTree(responseEvent.getBody());
        JsonNode oauthErrorNode = responseTreeRootNode.get("oauth_error");

        CommonExpressOAuthError expectedObject =
                new CommonExpressOAuthError(
                        OAuth2Error.ACCESS_DENIED, SESSION_NOT_FOUND.getMessage());

        assertNotNull(responseEvent);
        assertNotNull(responseTreeRootNode);
        assertNotNull(oauthErrorNode);
        assertEquals(HttpStatusCode.FORBIDDEN, responseEvent.getStatusCode());

        assertEquals(
                "oauth_error",
                responseTreeRootNode.fieldNames().next().toString()); // Root Node Name
        assertEquals(
                expectedObject.getError().get("error"),
                oauthErrorNode.get("error").textValue()); // error
        assertEquals(
                expectedObject.getError().get("error_description"),
                oauthErrorNode.get("error_description").textValue()); // error description
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
