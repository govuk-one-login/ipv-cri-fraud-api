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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.lambda.powertools.logging.CorrelationIdPathConstants;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.service.*;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.exception.CredentialRequestException;
import uk.gov.di.ipv.cri.fraud.api.persistence.item.FraudResultItem;
import uk.gov.di.ipv.cri.fraud.api.service.FraudRetrievalService;
import uk.gov.di.ipv.cri.fraud.api.service.VerifiableCredentialService;
import uk.gov.di.ipv.cri.fraud.api.util.IssueCredentialFraudAuditExtensionUtil;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.apache.logging.log4j.Level.ERROR;

public class IssueCredentialHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOGGER = LogManager.getLogger();

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
        ConfigurationService configurationService = new ConfigurationService();
        this.verifiableCredentialService = getVerifiableCredentialService(configurationService);
        this.personIdentityService = new PersonIdentityService();
        this.sessionService = new SessionService();
        this.eventProbe = new EventProbe();
        this.auditService =
                new AuditService(
                        SqsClient.builder().build(),
                        configurationService,
                        new ObjectMapper(),
                        new AuditEventFactory(configurationService, Clock.systemUTC()));
        this.fraudRetrievalService = new FraudRetrievalService();
    }

    @Override
    @Logging(correlationIdPath = CorrelationIdPathConstants.API_GATEWAY_REST)
    @Metrics(captureColdStart = true)
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        try {
            LOGGER.info(
                    "Initiating lambda {} version {}",
                    context.getFunctionName(),
                    context.getFunctionVersion());

            LOGGER.info("Validating authorization token...");
            var accessToken = validateInputHeaderBearerToken(input.getHeaders());
            var sessionItem = this.sessionService.getSessionByAccessToken(accessToken);
            LOGGER.info("Extracted session from session store ID {}", sessionItem.getSessionId());

            LOGGER.info("Retrieving identity details and fraud results...");
            var personIdentity =
                    personIdentityService.getPersonIdentityDetailed(sessionItem.getSessionId());
            FraudResultItem fraudResult =
                    fraudRetrievalService.getFraudResult(sessionItem.getSessionId());
            LOGGER.info("VC content retrieved.");

            LOGGER.info("Generating verifiable credential...");
            SignedJWT signedJWT =
                    verifiableCredentialService.generateSignedVerifiableCredentialJwt(
                            sessionItem.getSubject(), fraudResult, personIdentity);
            auditService.sendAuditEvent(
                    AuditEventType.VC_ISSUED,
                    IssueCredentialFraudAuditExtensionUtil.generateVCISSFraudAuditExtension(
                            verifiableCredentialService.getVerifiableCredentialIssuer(),
                            List.of(fraudResult)));
            eventProbe.counterMetric(FRAUD_CREDENTIAL_ISSUER, 0d);

            LOGGER.info("Credential generated");
            return ApiGatewayResponseGenerator.proxyJwtResponse(
                    HttpStatusCode.OK, signedJWT.serialize());
        } catch (AwsServiceException ex) {
            LOGGER.warn(
                    "Exception while handling lambda {} exception {}",
                    context.getFunctionName(),
                    ex.getClass());
            eventProbe.log(ERROR, ex).counterMetric(FRAUD_CREDENTIAL_ISSUER, 0d);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ex.awsErrorDetails().errorMessage());
        } catch (CredentialRequestException | ParseException | JOSEException e) {
            LOGGER.warn(
                    "Exception while handling lambda {} exception {}",
                    context.getFunctionName(),
                    e.getClass());
            eventProbe.log(ERROR, e).counterMetric(FRAUD_CREDENTIAL_ISSUER, 0d);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.BAD_REQUEST, ErrorResponse.VERIFIABLE_CREDENTIAL_ERROR);
        } catch (SqsException sqsException) {
            LOGGER.error(
                    "Exception while handling lambda {} exception {}",
                    context.getFunctionName(),
                    sqsException.getClass());
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, sqsException.getMessage());
        } catch (Exception e) {
            LOGGER.error(
                    "Exception while handling lambda {} exception {}",
                    context.getFunctionName(),
                    e.getClass());
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, e.getMessage());
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

    private VerifiableCredentialService getVerifiableCredentialService(
            ConfigurationService configurationService) {
        Supplier<VerifiableCredentialService> factory =
                () -> new VerifiableCredentialService(configurationService);
        return factory.get();
    }
}
