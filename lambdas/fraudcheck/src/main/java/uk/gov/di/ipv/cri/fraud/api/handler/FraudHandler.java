package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpException;
import org.apache.logging.log4j.Level;
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
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.service.ConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.fraud.api.service.ServiceFactory;
import uk.gov.di.ipv.cri.fraud.api.util.FraudPersonIdentityDetailedMapper;
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
    private final ConfigurationService configurationService;
    private final AuditService auditService;

    public FraudHandler() throws NoSuchAlgorithmException, InvalidKeyException, HttpException {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ServiceFactory serviceFactory = new ServiceFactory(objectMapper);
        this.eventProbe = new EventProbe();
        this.identityVerificationService = serviceFactory.getIdentityVerificationService();
        this.personIdentityService = new PersonIdentityService();
        this.sessionService = new SessionService();
        this.configurationService = serviceFactory.getConfigurationService();
        this.dataStore =
                new DataStore<>(
                        configurationService.getFraudResultTableName(),
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
            ConfigurationService configurationService,
            AuditService auditService) {
        this.identityVerificationService = serviceFactory.getIdentityVerificationService();
        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;
        this.personIdentityService = personIdentityService;
        this.sessionService = sessionService;
        this.configurationService = configurationService;
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
            var sessionItem = sessionService.validateSessionId(sessionId);

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
                            FraudPersonIdentityDetailedMapper.generatePersonIdentityDetailed(
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
            fraudResultItem.setTtl(configurationService.getFraudResultItemExpirationEpoch());
            fraudResultItem.setTransactionId(result.getTransactionId());
            fraudResultItem.setPepTransactionId(result.getPepTransactionId());

            fraudResultItem.setCheckDetails(result.getChecksSucceeded());
            fraudResultItem.setFailedCheckDetails(result.getChecksFailed());

            fraudResultItem.setActivityFrom(result.getActivityFrom());

            LOGGER.info("Saving fraud results...");
            dataStore.create(fraudResultItem);
            LOGGER.info("Fraud results saved.");

            // Lambda Complete No Error
            eventProbe.counterMetric(LAMBDA_IDENTITY_CHECK_COMPLETED_OK);

            return ApiGatewayResponseGenerator.proxyJsonResponse(HttpStatusCode.OK, result);
        } catch (Exception e) {
            LOGGER.warn("Exception while handling lambda {}", context.getFunctionName());
            eventProbe.log(Level.ERROR, e).counterMetric(LAMBDA_IDENTITY_CHECK_COMPLETED_ERROR);
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.GENERIC_SERVER_ERROR);
        }
    }
}
