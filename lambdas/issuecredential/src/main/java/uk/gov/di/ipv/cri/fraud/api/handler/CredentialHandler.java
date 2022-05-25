package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.Level;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.common.library.util.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.fraud.api.service.ServiceFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class CredentialHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String LAMBDA_NAME = "fraud_issue_credential";

    private final IdentityVerificationService identityVerificationService;
    private final ObjectMapper objectMapper;
    private final EventProbe eventProbe;

    public CredentialHandler(
            ServiceFactory serviceFactory, ObjectMapper objectMapper, EventProbe eventProbe) {
        this.identityVerificationService = serviceFactory.getIdentityVerificationService();
        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;
    }

    @ExcludeFromGeneratedCoverageReport
    public CredentialHandler() throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventProbe = new EventProbe();
        this.identityVerificationService =
                new ServiceFactory(this.objectMapper).getIdentityVerificationService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        try {
            PersonIdentity personIdentity =
                    objectMapper.readValue(input.getBody(), PersonIdentity.class);

            IdentityVerificationResult result =
                    identityVerificationService.verifyIdentity(personIdentity);

            if (!result.isSuccess()) {
                return ApiGatewayResponseGenerator.proxyJsonResponse(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        Map.of("error_description", result.getError()));
            }

            eventProbe.counterMetric(LAMBDA_NAME);
            return ApiGatewayResponseGenerator.proxyJsonResponse(HttpStatusCode.OK, result);
        } catch (Exception e) {
            eventProbe.log(Level.ERROR, e).counterMetric(LAMBDA_NAME, 0d);
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.GENERIC_SERVER_ERROR);
        }
    }
}
