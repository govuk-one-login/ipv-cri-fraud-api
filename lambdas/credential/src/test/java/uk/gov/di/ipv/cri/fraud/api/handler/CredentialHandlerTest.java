package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.fraud.api.service.ServiceFactory;
import uk.gov.di.ipv.cri.fraud.library.service.AccessTokenService;
import uk.gov.di.ipv.cri.fraud.library.validation.ValidationResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialHandlerTest {
    @Mock private AccessTokenService mockAccessTokenService;
    @Mock private ServiceFactory mockServiceFactory;
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private IdentityVerificationService mockIdentityVerificationService;

    private CredentialHandler credentialHandler;

    @BeforeEach
    void setup() {
        when(mockServiceFactory.getIdentityVerificationService())
                .thenReturn(mockIdentityVerificationService);
        this.credentialHandler =
                new CredentialHandler(mockAccessTokenService, mockServiceFactory, mockObjectMapper);
    }

    @Test
    void handleResponseShouldReturnOkResponseWhenValidInputProvided()
            throws JsonProcessingException {
        String testAuthorizationHeaderValue = "authorisation-header-value";
        String testRequestBody = "request body";
        PersonIdentity testPersonIdentity = new PersonIdentity();
        IdentityVerificationResult testIdentityVerificationResult =
                new IdentityVerificationResult();
        testIdentityVerificationResult.setSuccess(true);
        testIdentityVerificationResult.setContraIndicators(new String[] {"A01"});
        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);
        Map<String, String> requestHeaders = Map.of("Authorization", testAuthorizationHeaderValue);

        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);
        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        when(mockAccessTokenService.validateAccessToken(testAuthorizationHeaderValue))
                .thenReturn(ValidationResult.createValidResult());
        when(mockObjectMapper.readValue(testRequestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);
        when(mockIdentityVerificationService.verifyIdentity(testPersonIdentity))
                .thenReturn(testIdentityVerificationResult);

        APIGatewayProxyResponseEvent responseEvent =
                credentialHandler.handleRequest(mockRequestEvent, null);

        assertNotNull(responseEvent);
        assertEquals(HttpStatusCode.OK, responseEvent.getStatusCode());
        assertEquals(
                "{\"success\":true,\"validationErrors\":null,\"error\":null,\"contraIndicators\":[\"A01\"]}",
                responseEvent.getBody());
    }

    @Test
    void handleResponseShouldReturnUnauthorisedResponseWhenInvalidAccessTokenProvided()
            throws JsonProcessingException {
        String testAuthorizationHeaderValue = "authorisation-header-value";
        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);
        Map<String, String> requestHeaders = Map.of("Authorization", testAuthorizationHeaderValue);
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);
        when(mockAccessTokenService.validateAccessToken(testAuthorizationHeaderValue))
                .thenReturn(new ValidationResult<>(false, OAuth2Error.INVALID_REQUEST));

        APIGatewayProxyResponseEvent responseEvent =
                credentialHandler.handleRequest(mockRequestEvent, null);

        assertNotNull(responseEvent);
        assertEquals(HttpStatusCode.BAD_REQUEST, responseEvent.getStatusCode());
        assertEquals(
                "{\"error_description\":\"Invalid request\",\"error\":\"invalid_request\"}",
                responseEvent.getBody());
    }

    @Test
    void handleResponseShouldReturnInternalServerErrorResponseWhenUnableToContactThirdPartyApi()
            throws JsonProcessingException {
        String testAuthorizationHeaderValue = "authorisation-header-value";
        String testRequestBody = "request body";
        String errorMessage = "error message";
        PersonIdentity testPersonIdentity = new PersonIdentity();
        IdentityVerificationResult testIdentityVerificationResult =
                new IdentityVerificationResult();
        testIdentityVerificationResult.setSuccess(false);
        testIdentityVerificationResult.setError(errorMessage);
        APIGatewayProxyRequestEvent mockRequestEvent =
                Mockito.mock(APIGatewayProxyRequestEvent.class);
        Map<String, String> requestHeaders = Map.of("Authorization", testAuthorizationHeaderValue);
        when(mockRequestEvent.getHeaders()).thenReturn(requestHeaders);
        when(mockRequestEvent.getBody()).thenReturn(testRequestBody);
        when(mockAccessTokenService.validateAccessToken(testAuthorizationHeaderValue))
                .thenReturn(ValidationResult.createValidResult());
        when(mockObjectMapper.readValue(testRequestBody, PersonIdentity.class))
                .thenReturn(testPersonIdentity);
        when(mockIdentityVerificationService.verifyIdentity(testPersonIdentity))
                .thenReturn(testIdentityVerificationResult);

        APIGatewayProxyResponseEvent responseEvent =
                credentialHandler.handleRequest(mockRequestEvent, null);

        assertNotNull(responseEvent);
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR, responseEvent.getStatusCode());
        assertEquals("{\"error_description\":\"" + errorMessage + "\"}", responseEvent.getBody());
    }
}
