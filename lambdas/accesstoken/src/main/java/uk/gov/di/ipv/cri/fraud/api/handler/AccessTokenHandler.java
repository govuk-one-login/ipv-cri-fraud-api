package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.fraud.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.exception.AccessTokenProcessingException;
import uk.gov.di.ipv.cri.fraud.library.exception.AccessTokenValidationException;
import uk.gov.di.ipv.cri.fraud.library.helpers.ApiGatewayResponseGenerator;
import uk.gov.di.ipv.cri.fraud.library.service.AccessTokenService;

public class AccessTokenHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final AccessTokenService accessTokenService;

    public AccessTokenHandler(AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    @ExcludeFromGeneratedCoverageReport
    public AccessTokenHandler() {
        this.accessTokenService = new AccessTokenService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        try {
            TokenRequest tokenRequest = accessTokenService.createTokenRequest(input);

            AccessTokenResponse accessTokenResponse =
                    accessTokenService.createAndSaveAccessToken(tokenRequest);

            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.OK, accessTokenResponse.toJSONObject());

        } catch (AccessTokenValidationException e) {
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.BAD_REQUEST, ErrorResponse.INVALID_TOKEN_REQUEST);
        } catch (AccessTokenProcessingException e) {
            return ApiGatewayResponseGenerator.proxyJsonResponse(
                    HttpStatusCode.INTERNAL_SERVER_ERROR, ErrorResponse.SERVER_ERROR);
        }
    }
}
