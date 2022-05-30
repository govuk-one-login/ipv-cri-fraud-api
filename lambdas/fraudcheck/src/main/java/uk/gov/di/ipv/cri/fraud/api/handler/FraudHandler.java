package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.fraud.api.service.ServiceFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

public class FraudHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String LAMBDA_NAME = "fraud_issue_credential";

    private final IdentityVerificationService identityVerificationService;
    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;
    private final PersonIdentityService personIdentityService;

    public FraudHandler(
            ServiceFactory serviceFactory,
            ObjectMapper objectMapper,
            EventProbe eventProbe,
            PersonIdentityService personIdentityService) {
        this.identityVerificationService = serviceFactory.getIdentityVerificationService();
        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;
        this.personIdentityService = personIdentityService;
    }

    @ExcludeFromGeneratedCoverageReport
    public FraudHandler() throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProbe = new EventProbe();
        this.identityVerificationService =
                new ServiceFactory(this.objectMapper).getIdentityVerificationService();
        this.personIdentityService = new PersonIdentityService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        try {
            LOGGER.info(
                    "Initiating lambda {} version",
                    context.getFunctionName(),
                    context.getFunctionVersion());
            Map<String, String> headers = input.getHeaders();
            final String sessionId = headers.get("session_id");
            LOGGER.info("Extracted session from header {}", sessionId);

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
                    identityVerificationService.verifyIdentity(personIdentity);

            if (!result.isSuccess()) {
                LOGGER.info("Third party failed to assert identity. Error {}", result.getError());

                return ApiGatewayResponseGenerator.proxyJsonResponse(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        Map.of("error_description", result.getError()));
            }
            LOGGER.info("Identity verified");

            eventProbe.counterMetric(LAMBDA_NAME);
            return ApiGatewayResponseGenerator.proxyJsonResponse(HttpStatusCode.OK, result);
        } catch (Exception e) {
            LOGGER.error("Exception while handling lambda {}", context.getFunctionName(), e);
            eventProbe.log(Level.ERROR, e).counterMetric(LAMBDA_NAME, 0d);
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.GENERIC_SERVER_ERROR);
        }
    }
}
