package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import org.apache.http.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.lambda.powertools.logging.CorrelationIdPathConstants;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyPepGateway;
import uk.gov.di.ipv.cri.fraud.api.service.ActivityHistoryScoreCalculator;
import uk.gov.di.ipv.cri.fraud.api.service.ContraIndicatorMapper;
import uk.gov.di.ipv.cri.fraud.api.service.FraudCheckConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.fraud.api.service.PersonIdentityValidator;
import uk.gov.di.ipv.cri.fraud.api.service.ThirdPartyAPIServiceFactory;
import uk.gov.di.ipv.cri.fraud.api.util.RequestSentAuditHelper;
import uk.gov.di.ipv.cri.fraud.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.fraud.library.error.CommonExpressOAuthError;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.fraud.library.metrics.Definitions;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;
import uk.gov.di.ipv.cri.fraud.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.fraud.library.service.ResultItemStorageService;
import uk.gov.di.ipv.cri.fraud.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;
import uk.gov.di.ipv.cri.fraud.library.util.SleepHelper;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class FraudHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    // We need this first and static for it to be created as soon as possible during function init
    private static final long FUNCTION_INIT_START_TIME_MILLISECONDS = System.currentTimeMillis();

    private static final Logger LOGGER = LogManager.getLogger();

    private static final boolean DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG =
            Boolean.parseBoolean(System.getenv("DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG"));

    private EventProbe eventProbe;

    private SessionService sessionService;
    private AuditService auditService;

    private PersonIdentityService personIdentityService;

    private IdentityVerificationService identityVerificationService;

    private ResultItemStorageService<FraudResultItem> fraudResultItemStorageService;

    // Max Wait is FRAUD timeout + PEP timeout + some time for processing a result
    private static final int MAX_ATTEMPT_DUPLICATE_CHECK_WAIT_DURATION_MS =
            1000
                    + ThirdPartyFraudGateway.FRAUD_HTTP_RESPONSE_TIMEOUT_MS
                    + ThirdPartyPepGateway.PEP_HTTP_RESPONSE_TIMEOUT_MS;
    // To avoid waiting the full MAX_ATTEMPT_DUPLICATE_CHECK_WAIT_DURATION,
    // The lambda will recheck for a result at this interval
    private static final int DUPLICATE_CHECK_POLLING_INTERVAL_MS = 1000;

    private long fraudResultItemTtl;

    private long functionInitMetricLatchedValue = 0;
    private boolean functionInitMetricCaptured = false;

    @ExcludeFromGeneratedCoverageReport
    public FraudHandler() throws HttpException, JsonProcessingException {
        ServiceFactory serviceFactory = new ServiceFactory();

        FraudCheckConfigurationService fraudCheckConfigurationServiceNotYetAssigned =
                createFraudCheckConfigurationService(serviceFactory);

        IdentityVerificationService identityVerificationServiceNotAssignedYet =
                createIdentityVerificationService(
                        serviceFactory, fraudCheckConfigurationServiceNotYetAssigned);

        initializeLambdaServices(serviceFactory, identityVerificationServiceNotAssignedYet);
    }

    public FraudHandler(
            ServiceFactory serviceFactory,
            IdentityVerificationService identityVerificationService) {
        initializeLambdaServices(serviceFactory, identityVerificationService);
    }

    public void initializeLambdaServices(
            ServiceFactory serviceFactory,
            IdentityVerificationService identityVerificationService) {

        this.eventProbe = serviceFactory.getEventProbe();
        this.sessionService = serviceFactory.getSessionService();
        this.auditService = serviceFactory.getAuditService();
        this.personIdentityService = serviceFactory.getPersonIdentityService();

        ParameterStoreService parameterStoreService = serviceFactory.getParameterStoreService();

        this.fraudResultItemStorageService = serviceFactory.getResultItemStorageService();

        fraudResultItemTtl =
                Long.parseLong(
                        parameterStoreService.getParameterValue(
                                ParameterPrefix.COMMON_API,
                                ParameterStoreParameters.FRAUD_RESULT_ITEM_TTL_PARAMETER));

        this.identityVerificationService = identityVerificationService;

        // Runtime/SnapStart function init duration
        functionInitMetricLatchedValue =
                System.currentTimeMillis() - FUNCTION_INIT_START_TIME_MILLISECONDS;
    }

    private IdentityVerificationService createIdentityVerificationService(
            ServiceFactory serviceFactory,
            FraudCheckConfigurationService fraudCheckConfigurationService)
            throws HttpException {

        final ThirdPartyAPIServiceFactory thirdPartyAPIServiceFactory =
                new ThirdPartyAPIServiceFactory(serviceFactory, fraudCheckConfigurationService);

        final ActivityHistoryScoreCalculator activityHistoryScoreCalculator =
                new ActivityHistoryScoreCalculator();

        final PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        final ContraIndicatorMapper contraindicationMapper =
                new ContraIndicatorMapper(fraudCheckConfigurationService);

        return new IdentityVerificationService(
                serviceFactory,
                thirdPartyAPIServiceFactory,
                personIdentityValidator,
                contraindicationMapper,
                activityHistoryScoreCalculator,
                fraudCheckConfigurationService);
    }

    private FraudCheckConfigurationService createFraudCheckConfigurationService(
            ServiceFactory serviceFactory) throws JsonProcessingException {

        ParameterStoreService parameterStoreService = serviceFactory.getParameterStoreService();

        return new FraudCheckConfigurationService(parameterStoreService);
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
                        Definitions.LAMBDA_FRAUD_CHECK_FUNCTION_INIT_DURATION,
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

            Map<String, String> headers = input.getHeaders();
            final String sessionId = headers.get("session_id");
            LOGGER.info("Extracting session from header ID {}", sessionId);
            SessionItem sessionItem = sessionService.validateSessionId(sessionId);

            // Prevents users, who have initiated a check, starting a new check when one is in
            // progress or completed
            boolean attemptIsDuplicate = detectAndHandleDuplicateCheckAttempts(sessionItem);
            if (attemptIsDuplicate) {
                return completedOk();
            }

            PersonIdentity personIdentity =
                    personIdentityService.getPersonIdentity(sessionItem.getSessionId());
            if (null == personIdentity) {
                String message =
                        String.format(
                                "Could not retrieve person identity for session %s",
                                sessionItem.getSessionId());

                LOGGER.error(message);

                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.FAILED_TO_RETRIEVE_PERSON_IDENTITY);
            }

            LOGGER.info("Verifying identity...");
            IdentityVerificationResult identityVerificationResult =
                    identityVerificationService.verifyIdentity(
                            personIdentity, sessionItem, headers);

            auditService.sendAuditEvent(
                    AuditEventType.REQUEST_SENT,
                    new AuditEventContext(
                            RequestSentAuditHelper.personIdentityToAuditRestrictedFormat(
                                    personIdentity),
                            input.getHeaders(),
                            sessionItem));

            if (!identityVerificationResult.isSuccess()) {
                LOGGER.info(
                        "Third party failed to assert identity. Error {}",
                        identityVerificationResult.getError());

                if (identityVerificationResult.getError().equals("PersonIdentityValidationError")) {
                    String errorLogMessage =
                            String.join(",", identityVerificationResult.getValidationErrors());
                    LOGGER.error(errorLogMessage);
                }

                // Caught below in the common OAuthErrorResponseException handler
                throw new OAuthErrorResponseException(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        ErrorResponse.IDENTITY_VERIFICATION_UNSUCCESSFUL);
            }
            LOGGER.info("Identity verified.");

            LOGGER.info("Generating authorization code...");
            sessionService.createAuthorizationCode(sessionItem);

            LOGGER.info("Saving fraud results...");
            FraudResultItem fraudResultItem =
                    createFraudResultItem(identityVerificationResult, sessionItem);
            fraudResultItemStorageService.saveResultItem(fraudResultItem);
            LOGGER.info("Fraud results saved.");

            return completedOk();
        } catch (OAuthErrorResponseException e) {
            // Fraud Check Lambda Completed with an Error
            eventProbe.counterMetric(Definitions.LAMBDA_IDENTITY_CHECK_COMPLETED_ERROR);

            CommonExpressOAuthError commonExpressOAuthError;

            if (!DEV_ENVIRONMENT_ONLY_ENHANCED_DEBUG) {
                // Standard oauth compliant route
                commonExpressOAuthError = new CommonExpressOAuthError(OAuth2Error.SERVER_ERROR);
            } else {
                // Debug in DEV only as Oauth errors appear in the redirect url
                // This will output the specific error message
                // Note Unit tests expect server error (correctly)
                // and will fail if this is set (during unit tests)
                String customOAuth2ErrorDescription = e.getErrorReason();

                commonExpressOAuthError =
                        new CommonExpressOAuthError(
                                OAuth2Error.SERVER_ERROR, customOAuth2ErrorDescription);
            }

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    e.getStatusCode(), // Status Code determined by throw location
                    commonExpressOAuthError);
        } catch (Exception e) {
            // This is where unexpected exceptions will reach (null pointers etc)
            // We should not log unknown exceptions, due to possibility of PII
            LOGGER.error(
                    "Unhandled Exception while handling lambda {} exception {}",
                    context.getFunctionName(),
                    e.getClass());

            LOGGER.debug(e.getMessage(), e);

            eventProbe.counterMetric(Definitions.LAMBDA_IDENTITY_CHECK_COMPLETED_ERROR);

            // Oauth compliant response
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    new CommonExpressOAuthError(OAuth2Error.SERVER_ERROR));
        }
    }

    private FraudResultItem createFraudResultItem(
            IdentityVerificationResult identityVerificationResult, SessionItem sessionItem) {

        // Result for later use in VC generation
        final FraudResultItem fraudResultItem =
                new FraudResultItem(
                        sessionItem.getSessionId(),
                        identityVerificationResult.getContraIndicators(),
                        identityVerificationResult.getIdentityCheckScore(),
                        identityVerificationResult.getActivityHistoryScore(),
                        identityVerificationResult.getDecisionScore());

        // Expiry
        fraudResultItem.setTtl(
                Clock.systemUTC()
                        .instant()
                        .plus(fraudResultItemTtl, ChronoUnit.SECONDS)
                        .getEpochSecond());

        fraudResultItem.setTransactionId(identityVerificationResult.getTransactionId());
        fraudResultItem.setPepTransactionId(identityVerificationResult.getPepTransactionId());

        fraudResultItem.setCheckDetails(identityVerificationResult.getChecksSucceeded());
        fraudResultItem.setFailedCheckDetails(identityVerificationResult.getChecksFailed());

        fraudResultItem.setActivityFrom(identityVerificationResult.getActivityFrom());

        return fraudResultItem;
    }

    private APIGatewayProxyResponseEvent completedOk() {

        // Lambda Complete No Error
        eventProbe.counterMetric(Definitions.LAMBDA_IDENTITY_CHECK_COMPLETED_OK);

        return ApiGatewayResponseGenerator.proxyJsonResponse(HttpStatusCode.OK, null);
    }

    /**
     * Prevents users, who have initiated a check, starting a new check when one is in progress or
     * completed
     *
     * @param sessionItem
     * @return true if a duplicate attempt was handled, false if not a duplicate or a result was
     *     never found
     */
    private boolean detectAndHandleDuplicateCheckAttempts(SessionItem sessionItem) {
        // We need to update and save attempt count immediately to prevent additional checks
        // In FraudCRI the user is not slowed down by form data entry and submission
        sessionItem.setAttemptCount(sessionItem.getAttemptCount() + 1);
        sessionService.updateSession(sessionItem);
        LOGGER.info("Attempt Number {}", sessionItem.getAttemptCount());

        // If Attempt count is > 1,
        // Then another check, may be in progress or finished.
        // We prevent another remote API call and instead look for a result.
        if (sessionItem.getAttemptCount() > 1) {

            LOGGER.warn(
                    "Session attempt count value {} is greater than 1, a previous attempt may be in progress or have completed",
                    sessionItem.getAttemptCount());

            SleepHelper sleepHelper = new SleepHelper(DUPLICATE_CHECK_POLLING_INTERVAL_MS);
            boolean finished = false;
            long waitedTotalMilliseconds = 0;

            do {
                LOGGER.info("Searching for a completed result");
                FraudResultItem fraudResultItem =
                        fraudResultItemStorageService.getResultItem(sessionItem.getSessionId());

                if (fraudResultItem == null) {
                    LOGGER.info("No result found, waiting {}", DUPLICATE_CHECK_POLLING_INTERVAL_MS);

                    waitedTotalMilliseconds +=
                            sleepHelper.busyWaitMilliseconds(DUPLICATE_CHECK_POLLING_INTERVAL_MS);
                } else {

                    LOGGER.info("Completed result found");

                    // A duplicate attempt
                    return true;
                }

                // Exit condition to avoid waiting forever
                if (waitedTotalMilliseconds > MAX_ATTEMPT_DUPLICATE_CHECK_WAIT_DURATION_MS) {
                    finished = true;

                    // This could happen if the check in progress had an error and never completed
                    LOGGER.error(
                            "SessionId {} had a duplicate attempts (count {}) but a result was not found after waiting {}ms, allowing the current attempt to continue",
                            sessionItem.getSessionId(),
                            sessionItem.getAttemptCount(),
                            waitedTotalMilliseconds);
                }

            } while (!finished);
        }

        // Not a duplicate attempt (or original check had an error)
        return false;
    }
}
