package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.lambda.powertools.logging.CorrelationIdPathConstants;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.common.library.exception.SessionNotFoundException;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.common.library.util.KMSSigner;
import uk.gov.di.ipv.cri.fraud.api.exception.CredentialRequestException;
import uk.gov.di.ipv.cri.fraud.api.service.VerifiableCredentialService;
import uk.gov.di.ipv.cri.fraud.api.util.IssueCredentialFraudAuditExtensionUtil;
import uk.gov.di.ipv.cri.fraud.library.error.CommonExpressOAuthError;
import uk.gov.di.ipv.cri.fraud.library.metrics.Definitions;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;
import uk.gov.di.ipv.cri.fraud.library.service.ResultItemStorageService;
import uk.gov.di.ipv.cri.fraud.library.service.ServiceFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.di.ipv.cri.common.library.error.ErrorResponse.SESSION_NOT_FOUND;

public class IssueCredentialHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    // We need this first and static for it to be created as soon as possible during function init
    private static final long FUNCTION_INIT_START_TIME_MILLISECONDS = System.currentTimeMillis();

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    public static final String LAMBDA_HANDLING_EXCEPTION =
            "Exception while handling lambda {} exception {}";

    // CommonLib
    private ConfigurationService commonLibConfigurationService;
    private EventProbe eventProbe;
    private SessionService sessionService;
    private AuditService auditService;
    private PersonIdentityService personIdentityService;

    private ResultItemStorageService<FraudResultItem> fraudResultItemStorageService;
    private VerifiableCredentialService verifiableCredentialService;

    private long functionInitMetricLatchedValue = 0;
    private boolean functionInitMetricCaptured = false;

    @ExcludeFromGeneratedCoverageReport
    public IssueCredentialHandler() {
        ServiceFactory serviceFactory = new ServiceFactory();

        KMSSigner kmsSigner =
                new KMSSigner(
                        serviceFactory
                                .getCommonLibConfigurationService()
                                .getCommonParameterValue("verifiableCredentialKmsSigningKeyId"),
                        serviceFactory.getClientFactoryService().getKMSClient());

        // VerifiableCredentialService is internal to IssueCredentialHandler
        VerifiableCredentialService verifiableCredentialServiceNotAssignedYet =
                new VerifiableCredentialService(serviceFactory, kmsSigner);

        initializeLambdaServices(serviceFactory, verifiableCredentialServiceNotAssignedYet);
    }

    public IssueCredentialHandler(
            ServiceFactory serviceFactory,
            VerifiableCredentialService verifiableCredentialService) {
        initializeLambdaServices(serviceFactory, verifiableCredentialService);
    }

    private void initializeLambdaServices(
            ServiceFactory serviceFactory,
            VerifiableCredentialService verifiableCredentialService) {
        this.commonLibConfigurationService = serviceFactory.getCommonLibConfigurationService();

        this.eventProbe = serviceFactory.getEventProbe();

        this.sessionService = serviceFactory.getSessionService();
        this.auditService = serviceFactory.getAuditService();

        this.personIdentityService = serviceFactory.getPersonIdentityService();

        this.fraudResultItemStorageService = serviceFactory.getResultItemStorageService();

        this.verifiableCredentialService = verifiableCredentialService;

        // Runtime/SnapStart function init duration
        functionInitMetricLatchedValue =
                System.currentTimeMillis() - FUNCTION_INIT_START_TIME_MILLISECONDS;
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

            // Recorded here as sending metrics during function init may fail depending on lambda
            // config
            if (!functionInitMetricCaptured) {
                eventProbe.counterMetric(
                        Definitions.LAMBDA_ISSUE_CREDENTIAL_FUNCTION_INIT_DURATION,
                        functionInitMetricLatchedValue);
                LOGGER.info("Lambda function init duration {}ms", functionInitMetricLatchedValue);
                functionInitMetricCaptured = true;
            }

            // Lambda Lifetime
            long runTimeDuration =
                    System.currentTimeMillis() - FUNCTION_INIT_START_TIME_MILLISECONDS;
            Duration duration = Duration.of(runTimeDuration, ChronoUnit.MILLIS);
            String formattedDuration =
                    String.format(
                            "%d:%02d:%02d",
                            duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
            LOGGER.info(
                    "Lambda {}, Lifetime duration {}, {}ms",
                    context.getFunctionName(),
                    formattedDuration,
                    runTimeDuration);

            LOGGER.info("Validating authorization token...");
            var accessToken = validateInputHeaderBearerToken(input.getHeaders());
            var sessionItem = this.sessionService.getSessionByAccessToken(accessToken);
            LOGGER.info("Extracted session from session store ID {}", sessionItem.getSessionId());

            LOGGER.info("Retrieving identity details and fraud results...");
            var personIdentityDetailed =
                    personIdentityService.getPersonIdentityDetailed(sessionItem.getSessionId());
            FraudResultItem fraudResult =
                    fraudResultItemStorageService.getResultItem(sessionItem.getSessionId());
            LOGGER.info("VC content retrieved.");

            LOGGER.info("Generating verifiable credential...");
            SignedJWT signedJWT =
                    verifiableCredentialService.generateSignedVerifiableCredentialJwt(
                            sessionItem.getSubject(), fraudResult, personIdentityDetailed);

            final String verifiableCredentialIssuer =
                    commonLibConfigurationService.getVerifiableCredentialIssuer();

            auditService.sendAuditEvent(
                    AuditEventType.VC_ISSUED,
                    new AuditEventContext(input.getHeaders(), sessionItem),
                    IssueCredentialFraudAuditExtensionUtil.generateVCISSFraudAuditExtension(
                            verifiableCredentialIssuer, List.of(fraudResult)));

            LOGGER.info("Credential generated");

            // Lambda Complete No Error
            eventProbe.counterMetric(Definitions.LAMBDA_ISSUE_CREDENTIAL_COMPLETED_OK);

            auditService.sendAuditEvent(
                    AuditEventType.END, new AuditEventContext(input.getHeaders(), sessionItem));

            return ApiGatewayResponseGenerator.proxyJwtResponse(
                    HttpStatusCode.OK, signedJWT.serialize());
        } catch (SessionNotFoundException e) {

            String customOAuth2ErrorDescription = SESSION_NOT_FOUND.getMessage();
            LOGGER.error(customOAuth2ErrorDescription);
            eventProbe.counterMetric(Definitions.LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR);

            LOGGER.debug(e.getMessage(), e);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.FORBIDDEN,
                    new CommonExpressOAuthError(
                            OAuth2Error.ACCESS_DENIED, customOAuth2ErrorDescription));
        } catch (AwsServiceException ex) {
            LOGGER.warn(LAMBDA_HANDLING_EXCEPTION, context.getFunctionName(), ex.getClass());

            eventProbe.counterMetric(Definitions.LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ex.awsErrorDetails().errorMessage());
        } catch (CredentialRequestException | ParseException | JOSEException e) {
            LOGGER.warn(LAMBDA_HANDLING_EXCEPTION, context.getFunctionName(), e.getClass());

            eventProbe.counterMetric(Definitions.LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.BAD_REQUEST, ErrorResponse.VERIFIABLE_CREDENTIAL_ERROR);
        } catch (SqsException sqsException) {
            LOGGER.error(
                    LAMBDA_HANDLING_EXCEPTION, context.getFunctionName(), sqsException.getClass());

            eventProbe.counterMetric(Definitions.LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, sqsException.getMessage());
        } catch (Exception e) {
            // This is where unexpected exceptions will reach (null pointers etc)
            // We should not log unknown exceptions, due to possibility of PII
            LOGGER.error(LAMBDA_HANDLING_EXCEPTION, context.getFunctionName(), e.getClass());

            LOGGER.debug(e.getMessage(), e);

            eventProbe.counterMetric(Definitions.LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR);

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
}
