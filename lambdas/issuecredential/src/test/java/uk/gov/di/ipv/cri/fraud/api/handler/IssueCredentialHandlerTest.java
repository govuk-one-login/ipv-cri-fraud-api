package uk.gov.di.ipv.cri.fraud.api.handler;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueCredentialHandlerTest {
    /*public static final String SUBJECT = "subject";
    @Mock private Context context;
    @Mock private VerifiableCredentialService mockVerifiableCredentialService;
    @Mock private SessionService mockSessionService;

    @Mock private AddressService mockAddressService;
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

        final UUID sessionId = UUID.randomUUID();
        CanonicalAddress address = new CanonicalAddress();
        address.setBuildingNumber("114");
        address.setStreetName("Wellington Street");
        address.setPostalCode("LS1 1BA");
        AddressItem addressItem = new AddressItem();
        List<CanonicalAddress> canonicalAddresses = List.of(address);

        SessionItem sessionItem = new SessionItem();
        sessionItem.setSubject(SUBJECT);
        sessionItem.setSessionId(sessionId);
        addressItem.setAddresses(canonicalAddresses);

        when(mockSessionService.getSessionByAccessToken(accessToken)).thenReturn(sessionItem);
        when(mockAddressService.getAddressItem(sessionId)).thenReturn(addressItem);
        when(mockVerifiableCredentialService.generateSignedVerifiableCredentialJwt(
                        SUBJECT, canonicalAddresses))
                .thenReturn(mock(SignedJWT.class));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSessionByAccessToken(accessToken);
        verify(mockAddressService).getAddressItem(sessionId);
        verify(mockVerifiableCredentialService)
                .generateSignedVerifiableCredentialJwt(SUBJECT, canonicalAddresses);
        verify(mockEventProbe).counterMetric(IssueCredentialHandler.FRAUD_CREDENTIAL_ISSUER, 0d);
        verify(mockAuditService).sendAuditEvent(AuditEventTypes.IPV_ADDRESS_CRI_VC_ISSUED);
        assertEquals(
                ContentType.APPLICATION_JWT.getType(), response.getHeaders().get("Content-Type"));
        assertEquals(HttpStatusCode.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowJOSEExceptionWhenGenerateVerifiableCredentialIsMalformed()
            throws JsonProcessingException, JOSEException, SqsException {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        AccessToken accessToken = new BearerAccessToken();
        event.withHeaders(
                Map.of(
                        IssueCredentialHandler.AUTHORIZATION_HEADER_KEY,
                        accessToken.toAuthorizationHeader()));
        setRequestBodyAsPlainJWT(event);
        setupEventProbeErrorBehaviour();
        var unExpectedJOSEException = new JOSEException("Unexpected JOSE object type: JWSObject");

        final UUID sessionId = UUID.randomUUID();
        CanonicalAddress address = new CanonicalAddress();
        address.setBuildingNumber("114");
        address.setStreetName("Wellington Street");
        address.setPostalCode("LS1 1BA");
        AddressItem addressItem = new AddressItem();
        List<CanonicalAddress> canonicalAddresses = List.of(address);

        SessionItem sessionItem = new SessionItem();
        sessionItem.setSubject(SUBJECT);
        sessionItem.setSessionId(sessionId);
        addressItem.setAddresses(canonicalAddresses);

        when(mockSessionService.getSessionByAccessToken(accessToken)).thenReturn(sessionItem);
        when(mockAddressService.getAddressItem(sessionId)).thenReturn(addressItem);
        when(mockVerifiableCredentialService.generateSignedVerifiableCredentialJwt(
                        SUBJECT, canonicalAddresses))
                .thenThrow(unExpectedJOSEException);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSessionByAccessToken(accessToken);
        verify(mockAddressService).getAddressItem(sessionId);
        verify(mockVerifiableCredentialService)
                .generateSignedVerifiableCredentialJwt(SUBJECT, canonicalAddresses);
        verify(mockEventProbe).log(Level.ERROR, unExpectedJOSEException);
        verify(mockEventProbe).counterMetric(IssueCredentialHandler.FRAUD_CREDENTIAL_ISSUER, 0d);
        verifyNoMoreInteractions(mockVerifiableCredentialService);
        verify(mockAuditService, never()).sendAuditEvent(any());
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
        verify(mockAuditService, never()).sendAuditEvent(any());
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
        verify(mockEventProbe).counterMetric(IssueCredentialHandler.FRAUD_CREDENTIAL_ISSUER, 0d);
        verify(mockAuditService, never()).sendAuditEvent(any());
        String responseBody = new ObjectMapper().readValue(response.getBody(), String.class);
        assertEquals(awsErrorDetails.sdkHttpResponse().statusCode(), response.getStatusCode());
        assertEquals(awsErrorDetails.errorMessage(), responseBody);
    }

    @Test
    void shouldThrowAWSExceptionWhenAServerErrorOccursDuringRetrievingAnAddressItemWithSessionId()
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

        final UUID sessionId = UUID.randomUUID();
        SessionItem mockSessionItem = mock(SessionItem.class);
        when(mockSessionItem.getSessionId()).thenReturn(sessionId);
        when(mockSessionService.getSessionByAccessToken(accessToken)).thenReturn(mockSessionItem);
        when(mockAddressService.getAddressItem(sessionId))
                .thenThrow(
                        AwsServiceException.builder()
                                .statusCode(500)
                                .awsErrorDetails(awsErrorDetails)
                                .build());

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        verify(mockSessionService).getSessionByAccessToken(accessToken);
        verify(mockAddressService).getAddressItem(sessionId);
        verify(mockEventProbe).counterMetric(IssueCredentialHandler.FRAUD_CREDENTIAL_ISSUER, 0d);
        verify(mockAuditService, never()).sendAuditEvent(any());
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
    }*/
}
