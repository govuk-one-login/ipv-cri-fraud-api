package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import uk.gov.di.ipv.cri.common.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyPepGateway;
import uk.gov.di.ipv.cri.fraud.api.service.FraudCheckConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.fraud.api.service.ServiceFactory;
import uk.gov.di.ipv.cri.fraud.api.util.RequestSentAuditHelper;
import uk.gov.di.ipv.cri.fraud.api.util.SleepHelper;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.LAMBDA_IDENTITY_CHECK_COMPLETED_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.LAMBDA_IDENTITY_CHECK_COMPLETED_OK;

public class FraudHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOGGER = LogManager.getLogger();
    private final IdentityVerificationService identityVerificationService;
    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;
    private final PersonIdentityService personIdentityService;
    private final SessionService sessionService;
    private final DataStore<FraudResultItem> dataStore;
    private final FraudCheckConfigurationService fraudCheckConfigurationService;
    private final AuditService auditService;

    // Max Wait is FRAUD timeout + PEP timeout + some time for processing a result
    private static final int MAX_ATTEMPT_DUPLICATE_CHECK_WAIT_DURATION_MS =
            1000
                    + ThirdPartyFraudGateway.FRAUD_HTTP_RESPONSE_TIMEOUT_MS
                    + ThirdPartyPepGateway.PEP_HTTP_RESPONSE_TIMEOUT_MS;
    // To avoid waiting the full MAX_ATTEMPT_DUPLICATE_CHECK_WAIT_DURATION,
    // The lambda will recheck for a result at this interval
    private static final int DUPLICATE_CHECK_POLLING_INTERVAL_MS = 1000;

    public FraudHandler() throws NoSuchAlgorithmException, InvalidKeyException, HttpException {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ServiceFactory serviceFactory = new ServiceFactory(objectMapper);
        this.eventProbe = new EventProbe();
        this.identityVerificationService = serviceFactory.getIdentityVerificationService();
        this.personIdentityService = new PersonIdentityService();
        this.sessionService = new SessionService();
        this.fraudCheckConfigurationService = serviceFactory.getFraudCheckConfigurationService();
        this.dataStore =
                new DataStore<>(
                        fraudCheckConfigurationService.getFraudResultTableName(),
                        FraudResultItem.class,
                        DataStore.getClient());
        this.auditService = serviceFactory.getAuditService();
    }

    @ExcludeFromGeneratedCoverageReport
    public FraudHandler(
            ServiceFactory serviceFactory,
            ObjectMapper objectMapper,
            EventProbe eventProbe,
            PersonIdentityService personIdentityService,
            SessionService sessionService,
            DataStore<FraudResultItem> dataStore,
            FraudCheckConfigurationService fraudCheckConfigurationService,
            AuditService auditService) {
        this.identityVerificationService = serviceFactory.getIdentityVerificationService();
        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;
        this.personIdentityService = personIdentityService;
        this.sessionService = sessionService;
        this.fraudCheckConfigurationService = fraudCheckConfigurationService;
        this.dataStore = dataStore;
        this.auditService = auditService;
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

            PersonIdentity personIdentity = null;
            if (null != sessionId) {
                personIdentity =
                        personIdentityService.getPersonIdentity(UUID.fromString(sessionId));
            }
            if (null == personIdentity) {
                LOGGER.info("Person not found for session {}", sessionId);
                personIdentity = objectMapper.readValue(input.getBody(), PersonIdentity.class);
            }

            LOGGER.info("Verifying identity...");
            IdentityVerificationResult result =
                    identityVerificationService.verifyIdentity(
                            personIdentity, sessionItem, headers);

            auditService.sendAuditEvent(
                    AuditEventType.REQUEST_SENT,
                    new AuditEventContext(
                            RequestSentAuditHelper.personIdentityToAuditRestrictedFormat(
                                    personIdentity),
                            input.getHeaders(),
                            sessionItem));
            if (!result.isSuccess()) {
                LOGGER.info("Third party failed to assert identity. Error {}", result.getError());

                if (result.getError().equals("PersonIdentityValidationError")) {
                    LOGGER.error(String.join(",", result.getValidationErrors()));
                }

                eventProbe.counterMetric(LAMBDA_IDENTITY_CHECK_COMPLETED_ERROR);

                return ApiGatewayResponseGenerator.proxyJsonResponse(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        Map.of("error_description", result.getError()));
            }
            LOGGER.info("Identity verified.");

            LOGGER.info("Generating authorization code...");
            sessionService.createAuthorizationCode(sessionItem);

            // Result for later use in VC generation
            final FraudResultItem fraudResultItem =
                    new FraudResultItem(
                            UUID.fromString(sessionId),
                            result.getContraIndicators(),
                            result.getIdentityCheckScore(),
                            result.getActivityHistoryScore(),
                            result.getDecisionScore());
            fraudResultItem.setTtl(
                    fraudCheckConfigurationService.getFraudResultItemExpirationEpoch());
            fraudResultItem.setTransactionId(result.getTransactionId());
            fraudResultItem.setPepTransactionId(result.getPepTransactionId());

            fraudResultItem.setCheckDetails(result.getChecksSucceeded());
            fraudResultItem.setFailedCheckDetails(result.getChecksFailed());

            fraudResultItem.setActivityFrom(result.getActivityFrom());

            LOGGER.info("Saving fraud results...");
            dataStore.create(fraudResultItem);
            LOGGER.info("Fraud results saved.");

            return completedOk();
        } catch (Exception e) {
            // This is where unexpected exceptions will reach (null pointers etc)
            // We should not log unknown exceptions, due to possibility of PII
            LOGGER.error(
                    "Unhandled Exception while handling lambda {} exception {}",
                    context.getFunctionName(),
                    e.getClass());

            LOGGER.debug(e.getMessage(), e);

            eventProbe.counterMetric(LAMBDA_IDENTITY_CHECK_COMPLETED_ERROR);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.GENERIC_SERVER_ERROR);
        }
    }

    private APIGatewayProxyResponseEvent completedOk() {

        // Lambda Complete No Error
        eventProbe.counterMetric(LAMBDA_IDENTITY_CHECK_COMPLETED_OK);

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
                        dataStore.getItem(sessionItem.getSessionId().toString());

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
