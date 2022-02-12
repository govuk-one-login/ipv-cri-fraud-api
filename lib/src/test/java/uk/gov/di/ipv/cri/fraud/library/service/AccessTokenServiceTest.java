package uk.gov.di.ipv.cri.fraud.library.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.library.exception.AccessTokenProcessingException;
import uk.gov.di.ipv.cri.fraud.library.exception.AccessTokenValidationException;
import uk.gov.di.ipv.cri.fraud.library.persistence.DataStore;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.AccessTokenItem;
import uk.gov.di.ipv.cri.fraud.library.validation.ValidationResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceTest {

    @Mock private DataStore<AccessTokenItem> mockDataStore;

    @Mock private ConfigurationService mockConfigurationService;

    @Captor private ArgumentCaptor<AccessTokenItem> mockAccessTokenItem;

    private AccessTokenService accessTokenService;

    @BeforeEach
    void setUp() {
        this.accessTokenService = new AccessTokenService(mockDataStore, mockConfigurationService);
    }

    @Test
    void shouldReturnSuccessfulTokenResponseOnSuccessfulExchange() throws Exception {
        long testTokenTtl = 2400L;
        Scope testScope = new Scope("test-scope");
        TokenRequest tokenRequest =
                new TokenRequest(
                        null,
                        new ClientID("test-client-id"),
                        new AuthorizationCodeGrant(
                                new AuthorizationCode("123456"), new URI("http://test.com")),
                        testScope);
        when(mockConfigurationService.getBearerAccessTokenTtl()).thenReturn(testTokenTtl);

        TokenResponse response = accessTokenService.generateAccessToken(tokenRequest);

        assertInstanceOf(AccessTokenResponse.class, response);
        assertNotNull(response.toSuccessResponse().getTokens().getAccessToken().getValue());
        assertEquals(
                testTokenTtl,
                response.toSuccessResponse().getTokens().getBearerAccessToken().getLifetime());
        assertEquals(
                testScope,
                response.toSuccessResponse().getTokens().getBearerAccessToken().getScope());
    }

    @Test
    void shouldReturnValidationErrorWhenInvalidGrantTypeProvided() {
        TokenRequest tokenRequest =
                new TokenRequest(
                        null,
                        new ClientID("test-client-id"),
                        new RefreshTokenGrant(new RefreshToken()));

        ValidationResult<ErrorObject> validationResult =
                accessTokenService.validateTokenRequest(tokenRequest);

        assertNotNull(validationResult);
        assertFalse(validationResult.isValid());
        assertEquals(OAuth2Error.UNSUPPORTED_GRANT_TYPE, validationResult.getError());
    }

    @Test
    void shouldNotReturnValidationErrorWhenAValidTokenRequestIsProvided()
            throws URISyntaxException {
        AuthorizationGrant clientGrant = new ClientCredentialsGrant();

        ClientID clientID = new ClientID("clientID");
        Secret clientSecret = new Secret("clientSecret");
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        URI tokenEndpoint = new URI("https://example.com/token");

        TokenRequest tokenRequest = new TokenRequest(tokenEndpoint, clientAuth, clientGrant, null);

        ValidationResult<ErrorObject> validationResult =
                accessTokenService.validateTokenRequest(tokenRequest);

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getError());
    }

    @Test
    void shouldPersistAccessToken() {
        String testResourceId = UUID.randomUUID().toString();
        AccessToken accessToken = new BearerAccessToken();
        AccessTokenResponse accessTokenResponse =
                new AccessTokenResponse(new Tokens(accessToken, null));
        ArgumentCaptor<AccessTokenItem> accessTokenItemArgCaptor =
                ArgumentCaptor.forClass(AccessTokenItem.class);

        accessTokenService.persistAccessToken(accessTokenResponse, testResourceId);

        verify(mockDataStore).create(accessTokenItemArgCaptor.capture());
        AccessTokenItem capturedAccessTokenItem = accessTokenItemArgCaptor.getValue();
        assertNotNull(capturedAccessTokenItem);
        assertEquals(testResourceId, capturedAccessTokenItem.getResourceId());
        assertEquals(
                accessTokenResponse.getTokens().getBearerAccessToken().toAuthorizationHeader(),
                capturedAccessTokenItem.getAccessToken());
    }

    @Test
    void shouldGetSessionIdByAccessTokenWhenValidAccessTokenProvided() {
        String testResourceId = UUID.randomUUID().toString();
        String accessToken = new BearerAccessToken().toAuthorizationHeader();

        AccessTokenItem accessTokenItem = new AccessTokenItem();
        accessTokenItem.setResourceId(testResourceId);
        when(mockDataStore.getItem(accessToken)).thenReturn(accessTokenItem);

        String resultIpvSessionId = accessTokenService.getResourceIdByAccessToken(accessToken);

        verify(mockDataStore).getItem(accessToken);

        assertNotNull(resultIpvSessionId);
        assertEquals(testResourceId, resultIpvSessionId);
    }

    @Test
    void shouldReturnNullWhenInvalidAccessTokenProvided() {
        String accessToken = new BearerAccessToken().toAuthorizationHeader();

        when(mockDataStore.getItem(accessToken)).thenReturn(null);

        String resultIpvSessionId = accessTokenService.getResourceIdByAccessToken(accessToken);

        verify(mockDataStore).getItem(accessToken);
        assertNull(resultIpvSessionId);
    }

    @DisplayName("should create TokenRequest with AuthGrant and ClientAuthentication")
    @Test
    void shouldCreateTokenRequest() throws AccessTokenValidationException {
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setMultiValueHeaders(makeHeadersMap());
        input.setBody("grant_type=client_credentials&scope=fraud-check");
        input.setHttpMethod("POST");
        TokenRequest tokenRequest = accessTokenService.createTokenRequest(input);
        ClientAuthentication clientAuthentication = tokenRequest.getClientAuthentication();
        AuthorizationGrant authorizationGrant = tokenRequest.getAuthorizationGrant();
        assertEquals("aladdin", clientAuthentication.getClientID().getValue());
        assertEquals(GrantType.CLIENT_CREDENTIALS, authorizationGrant.getType());
        assertEquals("fraud-check", tokenRequest.getScope().toString());
    }

    @DisplayName("TokenRequest not created when grant_type duplicated")
    @Test
    void shouldNotCreateTokenRequest() {
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setMultiValueHeaders(makeHeadersMap());
        input.setHttpMethod("POST");

        // under test
        input.setBody(
                "grant_type=client_credentials&grant_type=client_credentials&scope=fraud-check");

        assertThrows(
                AccessTokenValidationException.class,
                () -> {
                    accessTokenService.createTokenRequest(input);
                });
    }

    @Test
    void shouldSaveToken() throws AccessTokenValidationException, AccessTokenProcessingException {
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setMultiValueHeaders(makeHeadersMap());
        input.setBody("grant_type=client_credentials&scope=fraud-check");
        input.setHttpMethod("POST");
        TokenRequest tokenRequest = accessTokenService.createTokenRequest(input);
        accessTokenService.createAndSaveAccessToken(tokenRequest);
        verify(mockDataStore).create(mockAccessTokenItem.capture());
    }

    @Test
    void shouldNotSaveTokenOnDataStoreError() throws AccessTokenValidationException {

        doThrow(new RuntimeException()).when(mockDataStore).create(any(AccessTokenItem.class));
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setMultiValueHeaders(makeHeadersMap());
        input.setBody("grant_type=client_credentials&scope=fraud-check");
        input.setHttpMethod("POST");
        TokenRequest tokenRequest = accessTokenService.createTokenRequest(input);

        assertThrows(
                AccessTokenProcessingException.class,
                () -> {
                    accessTokenService.createAndSaveAccessToken(tokenRequest);
                });
    }

    @Test
    void shouldNotSaveTokenOnTokenValidationError() throws AccessTokenValidationException {
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
        input.setMultiValueHeaders(makeHeadersMap());
        input.setBody("grant_type=authorization_code&code=some_code");
        input.setHttpMethod("POST");
        TokenRequest tokenRequest = accessTokenService.createTokenRequest(input);

        assertThrows(
                AccessTokenValidationException.class,
                () -> {
                    accessTokenService.createAndSaveAccessToken(tokenRequest);
                    verifyNoInteractions(mockDataStore);
                });
    }

    private Map<String, List<String>> makeHeadersMap() {
        return Map.of(
                "Authorization",
                List.of(" Basic YWxhZGRpbjpvcGVuc2VzYW1l"),
                "Content-Type",
                List.of("application/x-www-form-urlencoded"));
    }
}
