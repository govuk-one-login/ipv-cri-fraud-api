package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.lambda.powertools.logging.CorrelationIdPathConstants;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventTypes;
import uk.gov.di.ipv.cri.common.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.exception.CredentialRequestException;
import uk.gov.di.ipv.cri.fraud.api.persistence.item.FraudResultItem;
import uk.gov.di.ipv.cri.fraud.api.service.FraudRetrievalService;
import uk.gov.di.ipv.cri.fraud.api.service.VerifiableCredentialService;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.apache.logging.log4j.Level.ERROR;

public class IssueCredentialHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    public static final String FRAUD_CREDENTIAL_ISSUER = "fraud_credential_issuer";
    private final VerifiableCredentialService verifiableCredentialService;
    private final PersonIdentityService personIdentityService;
    private final FraudRetrievalService fraudRetrievalService;
    private final SessionService sessionService;
    private EventProbe eventProbe;
    private final AuditService auditService;

    public IssueCredentialHandler(
            VerifiableCredentialService verifiableCredentialService,
            // AddressService addressService,
            SessionService sessionService,
            EventProbe eventProbe,
            AuditService auditService,
            PersonIdentityService personIdentityService,
            FraudRetrievalService fraudRetrievalService) {
        this.verifiableCredentialService = verifiableCredentialService;
        this.personIdentityService = personIdentityService;
        this.sessionService = sessionService;
        this.eventProbe = eventProbe;
        this.auditService = auditService;
        this.fraudRetrievalService = fraudRetrievalService;
    }

    public IssueCredentialHandler() {
        this.verifiableCredentialService = getVerifiableCredentialService();
        this.personIdentityService = new PersonIdentityService();
        this.sessionService = new SessionService();
        this.eventProbe = new EventProbe();
        this.auditService =
                new AuditService(
                        SqsClient.builder().build(),
                        new ConfigurationService(),
                        new ObjectMapper());
        this.fraudRetrievalService = new FraudRetrievalService();
    }

    @Override
    @Logging(correlationIdPath = CorrelationIdPathConstants.API_GATEWAY_REST)
    @Metrics(captureColdStart = true)
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        try {
            var accessToken = validateInputHeaderBearerToken(input.getHeaders());
            var sessionItem = this.sessionService.getSessionByAccessToken(accessToken);
            var personIdentity =
                    personIdentityService.getPersonIdentityDetailed(sessionItem.getSessionId());
            FraudResultItem fraudResult =
                    fraudRetrievalService.getFraudResult(sessionItem.getSessionId());

            SignedJWT signedJWT =
                    verifiableCredentialService.generateSignedVerifiableCredentialJwt(
                            sessionItem.getSubject(), fraudResult, personIdentity);
            auditService.sendAuditEvent(AuditEventTypes.IPV_FRAUD_CRI_VC_ISSUED);
            eventProbe.counterMetric(FRAUD_CREDENTIAL_ISSUER, 0d);

            return ApiGatewayResponseGenerator.proxyJwtResponse(
                    HttpStatusCode.OK, signedJWT.serialize());
        } catch (AwsServiceException ex) {
            eventProbe.log(ERROR, ex).counterMetric(FRAUD_CREDENTIAL_ISSUER, 0d);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ex.awsErrorDetails().errorMessage());
        } catch (CredentialRequestException | ParseException | JOSEException e) {
            eventProbe.log(ERROR, e).counterMetric(FRAUD_CREDENTIAL_ISSUER, 0d);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.BAD_REQUEST, ErrorResponse.VERIFIABLE_CREDENTIAL_ERROR);
        } catch (SqsException sqsException) {
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, sqsException.getMessage());
        }
    }

    private AccessToken validateInputHeaderBearerToken(Map<String, String> headers)
            throws CredentialRequestException, ParseException {
        var token =
                Optional.ofNullable(headers).stream()
                        .flatMap(x -> x.entrySet().stream())
                        .filter(
                                header ->
                                        AUTHORIZATION_HEADER_KEY.equalsIgnoreCase(header.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new CredentialRequestException(
                                                ErrorResponse.MISSING_AUTHORIZATION_HEADER));

        return AccessToken.parse(token, AccessTokenType.BEARER);
    }

    private VerifiableCredentialService getVerifiableCredentialService() {
        Supplier<VerifiableCredentialService> factory = VerifiableCredentialService::new;
        return factory.get();
    }
}
