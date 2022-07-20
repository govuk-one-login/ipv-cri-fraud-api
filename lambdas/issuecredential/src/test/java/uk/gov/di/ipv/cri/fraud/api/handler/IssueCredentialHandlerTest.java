package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.common.contenttype.ContentType;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.http.SdkHttpResponse;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.audit.VCISSFraudAuditExtension;
import uk.gov.di.ipv.cri.fraud.api.persistence.item.FraudResultItem;
import uk.gov.di.ipv.cri.fraud.api.service.FraudRetrievalService;
import uk.gov.di.ipv.cri.fraud.api.service.VerifiableCredentialService;
import uk.gov.di.ipv.cri.fraud.api.util.FraudPersonIdentityDetailedMapper;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.logging.log4j.Level.ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueCredentialHandlerTest {
    public static final String SUBJECT = "subject";
    @Mock private Context context;
    @Mock private VerifiableCredentialService mockVerifiableCredentialService;
    @Mock private SessionService mockSessionService;
    @Mock private PersonIdentityService mockPersonIdentityService;
    @Mock private FraudRetrievalService mockFraudRetrievalService;
    @Mock private EventProbe mockEventProbe;
    @Mock private AuditService mockAuditService;
    @InjectMocks private IssueCredentialHandler handler;

    @Test
    void shouldReturn200OkWhenIssueCredentialRequestIsValid() throws JOSEException, SqsException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        AccessToken accessToken = new BearerAccessToken();
        event.withHeaders(
                Map.of(
                        IssueCredentialHandler.AUTHORIZATION_HEADER_KEY,
                        accessToken.toAuthorizationHeader()));
        setRequestBodyAsPlainJWT(event);

        var personIdentityDetailed =
                FraudPersonIdentityDetailedMapper.generatePersonIdentityDetailed(
                        TestDataCreator.createTestPersonIdentity());
        SessionItem sessionItem = new SessionItem();
        FraudResultItem fraudResultItem = new FraudResultItem(UUID.randomUUID(), List.of(""), 1);

        when(mockSessionService.getSessionByAccessToken(accessToken)).thenReturn(sessionItem);
        when(mockPersonIdentityService.getPersonIdentityDetailed(any()))
                .thenReturn(personIdentityDetailed);
        when(mockFraudRetrievalService.getFraudResult(sessionItem.getSessionId()))
                .thenReturn(fraudResultItem);
        when(mockVerifiableCredentialService.generateSignedVerifiableCredentialJwt(
                        sessionItem.getSubject(), fraudResultItem, personIdentityDetailed))
                .thenReturn(mock(SignedJWT.class));
        doNothing()
                .when(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.VC_ISSUED), any(VCISSFraudAuditExtension.class));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSessionByAccessToken(accessToken);
        verify(mockFraudRetrievalService).getFraudResult(sessionItem.getSessionId());
        verify(mockPersonIdentityService).getPersonIdentityDetailed(any());
        verify(mockVerifiableCredentialService)
                .generateSignedVerifiableCredentialJwt(
                        sessionItem.getSubject(), fraudResultItem, personIdentityDetailed);
        verify(mockAuditService)
                .sendAuditEvent(eq(AuditEventType.VC_ISSUED), any(VCISSFraudAuditExtension.class));
        verify(mockEventProbe).counterMetric(IssueCredentialHandler.FRAUD_CREDENTIAL_ISSUER, 0d);
        assertEquals(
                ContentType.APPLICATION_JWT.getType(), response.getHeaders().get("Content-Type"));
        assertEquals(HttpStatusCode.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowJOSEExceptionWhenGenerateVerifiableCredentialIsMalformed()
            throws JsonProcessingException, JOSEException, SqsException, JsonProcessingException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        AccessToken accessToken = new BearerAccessToken();
        event.withHeaders(
                Map.of(
                        IssueCredentialHandler.AUTHORIZATION_HEADER_KEY,
                        accessToken.toAuthorizationHeader()));
        setRequestBodyAsPlainJWT(event);
        setupEventProbeErrorBehaviour();
        var unExpectedJOSEException = new JOSEException("Unexpected JOSE object type: JWSObject");

        var personIdentityDetailed =
                FraudPersonIdentityDetailedMapper.generatePersonIdentityDetailed(
                        TestDataCreator.createTestPersonIdentity());

        SessionItem sessionItem = new SessionItem();
        FraudResultItem fraudResultItem = new FraudResultItem(UUID.randomUUID(), List.of(""), 1);

        when(mockSessionService.getSessionByAccessToken(accessToken)).thenReturn(sessionItem);
        when(mockPersonIdentityService.getPersonIdentityDetailed(any()))
                .thenReturn(personIdentityDetailed);
        when(mockFraudRetrievalService.getFraudResult(sessionItem.getSessionId()))
                .thenReturn(fraudResultItem);
        when(mockVerifiableCredentialService.generateSignedVerifiableCredentialJwt(
                        sessionItem.getSubject(), fraudResultItem, personIdentityDetailed))
                .thenThrow(unExpectedJOSEException);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSessionByAccessToken(accessToken);
        verify(mockFraudRetrievalService).getFraudResult(sessionItem.getSessionId());
        verify(mockPersonIdentityService).getPersonIdentityDetailed(any());
        verify(mockVerifiableCredentialService)
                .generateSignedVerifiableCredentialJwt(
                        sessionItem.getSubject(), fraudResultItem, personIdentityDetailed);
        verify(mockEventProbe).counterMetric(IssueCredentialHandler.FRAUD_CREDENTIAL_ISSUER, 0d);
        verifyNoMoreInteractions(mockVerifiableCredentialService);
        verify(mockAuditService, never())
                .sendAuditEvent(eq(AuditEventType.VC_ISSUED), any(VCISSFraudAuditExtension.class));
        Map responseBody = new ObjectMapper().readValue(response.getBody(), Map.class);
        assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorResponse.VERIFIABLE_CREDENTIAL_ERROR.getCode(), responseBody.get("code"));
        assertEquals(
                ErrorResponse.VERIFIABLE_CREDENTIAL_ERROR.getMessage(),
                responseBody.get("message"));
    }

    @Test
    void shouldThrowCredentialRequestExceptionWhenAuthorizationHeaderIsNotSupplied()
            throws SqsException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        setupEventProbeErrorBehaviour();

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);
        verify(mockEventProbe).counterMetric(IssueCredentialHandler.FRAUD_CREDENTIAL_ISSUER, 0d);
        verify(mockAuditService, never())
                .sendAuditEvent(eq(AuditEventType.VC_ISSUED), any(VCISSFraudAuditExtension.class));
        assertEquals(
                ContentType.APPLICATION_JSON.getType(), response.getHeaders().get("Content-Type"));
        assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldThrowAWSExceptionWhenAServerErrorOccursRetrievingASessionItemWithAccessToken()
            throws JsonProcessingException, SqsException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        AccessToken accessToken = new BearerAccessToken();
        event.withHeaders(
                Map.of(
                        IssueCredentialHandler.AUTHORIZATION_HEADER_KEY,
                        accessToken.toAuthorizationHeader()));

        setRequestBodyAsPlainJWT(event);
        setupEventProbeErrorBehaviour();

        AwsErrorDetails awsErrorDetails =
                AwsErrorDetails.builder()
                        .errorCode("")
                        .sdkHttpResponse(
                                SdkHttpResponse.builder()
                                        .statusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                                        .build())
                        .errorMessage("AWS DynamoDbException Occurred")
                        .build();

        when(mockSessionService.getSessionByAccessToken(accessToken))
                .thenThrow(
                        AwsServiceException.builder()
                                .statusCode(500)
                                .awsErrorDetails(awsErrorDetails)
                                .build());

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSessionByAccessToken(accessToken);
        verify(mockPersonIdentityService, never()).getPersonIdentityDetailed(UUID.randomUUID());
        verify(mockAuditService, never())
                .sendAuditEvent(eq(AuditEventType.VC_ISSUED), any(VCISSFraudAuditExtension.class));
        verify(mockEventProbe).counterMetric(IssueCredentialHandler.FRAUD_CREDENTIAL_ISSUER, 0d);
        verify(mockAuditService, never()).sendAuditEvent((AuditEventType) any());
        String responseBody = new ObjectMapper().readValue(response.getBody(), String.class);
        assertEquals(awsErrorDetails.sdkHttpResponse().statusCode(), response.getStatusCode());
        assertEquals(awsErrorDetails.errorMessage(), responseBody);
    }

    @Test
    void shouldThrowAWSExceptionWhenAServerErrorOccursDuringRetrievingPersonIdentityWithSessionId()
            throws JsonProcessingException, SqsException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        AccessToken accessToken = new BearerAccessToken();
        event.withHeaders(
                Map.of(
                        IssueCredentialHandler.AUTHORIZATION_HEADER_KEY,
                        accessToken.toAuthorizationHeader()));

        setRequestBodyAsPlainJWT(event);
        setupEventProbeErrorBehaviour();

        AwsErrorDetails awsErrorDetails =
                AwsErrorDetails.builder()
                        .errorCode("")
                        .sdkHttpResponse(
                                SdkHttpResponse.builder()
                                        .statusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
                                        .build())
                        .errorMessage("AWS DynamoDbException Occurred")
                        .build();

        SessionItem sessionItem = new SessionItem();
        when(mockSessionService.getSessionByAccessToken(accessToken)).thenReturn(sessionItem);
        when(mockPersonIdentityService.getPersonIdentityDetailed(sessionItem.getSessionId()))
                .thenThrow(
                        AwsServiceException.builder()
                                .statusCode(500)
                                .awsErrorDetails(awsErrorDetails)
                                .build());

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSessionByAccessToken(accessToken);
        verify(mockPersonIdentityService).getPersonIdentityDetailed(sessionItem.getSessionId());
        verify(mockAuditService, never())
                .sendAuditEvent(eq(AuditEventType.VC_ISSUED), any(VCISSFraudAuditExtension.class));
        verify(mockEventProbe).log(eq(ERROR), any(AwsServiceException.class));
        verify(mockEventProbe).counterMetric(IssueCredentialHandler.FRAUD_CREDENTIAL_ISSUER, 0d);
        String responseBody = new ObjectMapper().readValue(response.getBody(), String.class);
        assertEquals(awsErrorDetails.sdkHttpResponse().statusCode(), response.getStatusCode());
        assertEquals(awsErrorDetails.errorMessage(), responseBody);
    }

    private void setupEventProbeErrorBehaviour() {
        when(mockEventProbe.counterMetric(anyString(), anyDouble())).thenReturn(mockEventProbe);
        when(mockEventProbe.log(any(Level.class), any(Exception.class))).thenReturn(mockEventProbe);
    }

    private void setRequestBodyAsPlainJWT(APIGatewayProxyRequestEvent event) {
        String requestJWT =
                new PlainJWT(
                                new JWTClaimsSet.Builder()
                                        .claim(JWTClaimNames.SUBJECT, SUBJECT)
                                        .build())
                        .serialize();

        event.setBody(requestJWT);
    }
}
