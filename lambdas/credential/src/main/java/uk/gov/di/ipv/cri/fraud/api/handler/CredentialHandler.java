package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.oauth2.sdk.ErrorObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.fraud.api.service.ServiceFactory;
import uk.gov.di.ipv.cri.fraud.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.helpers.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.fraud.library.helpers.RequestHelper;
import uk.gov.di.ipv.cri.fraud.library.service.AccessTokenService;
import uk.gov.di.ipv.cri.fraud.library.validation.ValidationResult;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class CredentialHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialHandler.class);

    private final AccessTokenService accessTokenService;
    private final IdentityVerificationService identityVerificationService;
    private final ObjectMapper objectMapper;

    public CredentialHandler(
            AccessTokenService accessTokenService,
            ServiceFactory serviceFactory,
            ObjectMapper objectMapper) {
        this.accessTokenService = accessTokenService;
        this.identityVerificationService = serviceFactory.getIdentityVerificationService();
        this.objectMapper = objectMapper;
    }

    @ExcludeFromGeneratedCoverageReport
    public CredentialHandler() throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        this.accessTokenService = new AccessTokenService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.identityVerificationService =
                new ServiceFactory(this.objectMapper).getIdentityVerificationService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        try {
            String accessTokenFromRequest =
                    RequestHelper.getHeaderByKey(input.getHeaders(), "Authorization");

            ValidationResult<ErrorObject> validationResult =
                    accessTokenService.validateAccessToken(accessTokenFromRequest);
            if (!validationResult.isValid()) {
                return ApiGatewayResponseGenerator.proxyJsonResponse(
                        validationResult.getError().getHTTPStatusCode(),
                        validationResult.getError().toJSONObject());
            }

            PersonIdentity personIdentity =
                    objectMapper.readValue(input.getBody(), PersonIdentity.class);

            IdentityVerificationResult result =
                    identityVerificationService.verifyIdentity(personIdentity);

            if (!result.isSuccess()) {
                return ApiGatewayResponseGenerator.proxyJsonResponse(
                        HttpStatusCode.INTERNAL_SERVER_ERROR,
                        Map.of("error_description", result.getError()));
            }

            return ApiGatewayResponseGenerator.proxyJsonResponse(HttpStatusCode.OK, result);
        } catch (Exception e) {
            LOGGER.error("Error occurred whilst handling request", e);
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.SERVER_ERROR);
        }
    }
}
