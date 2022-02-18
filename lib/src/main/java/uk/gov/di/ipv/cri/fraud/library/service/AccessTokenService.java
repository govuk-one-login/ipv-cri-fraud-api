package uk.gov.di.ipv.cri.fraud.library.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.ipv.cri.fraud.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.fraud.library.exception.AccessTokenProcessingException;
import uk.gov.di.ipv.cri.fraud.library.exception.AccessTokenValidationException;
import uk.gov.di.ipv.cri.fraud.library.persistence.DataStore;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.AccessTokenItem;
import uk.gov.di.ipv.cri.fraud.library.validation.ValidationResult;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AccessTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenService.class);

    private final DataStore<AccessTokenItem> dataStore;
    private final ConfigurationService configurationService;

    @ExcludeFromGeneratedCoverageReport
    public AccessTokenService() {
        this.configurationService = new ConfigurationService();
        this.dataStore =
                new DataStore<>(
                        this.configurationService.getAccessTokenTableName(),
                        AccessTokenItem.class,
                        DataStore.getClient());
    }

    public AccessTokenService(
            DataStore<AccessTokenItem> dataStore, ConfigurationService configurationService) {
        this.dataStore = dataStore;
        this.configurationService = configurationService;
    }

    public TokenRequest createTokenRequest(APIGatewayProxyRequestEvent input)
            throws AccessTokenValidationException {
        try {
            HTTPRequest request =
                    new HTTPRequest(
                            HTTPRequest.Method.valueOf(input.getHttpMethod()),
                            URI.create("https://gds"));
            String body = input.getBody();
            if (StringUtils.isNotBlank(body)) {
                request.setQuery(body.trim());
            }
            Map<String, List<String>> multiValueHeaders = input.getMultiValueHeaders();
            if (multiValueHeaders != null) {

                multiValueHeaders.forEach(
                        (key, values) -> {
                            if (key != null) {
                                request.setHeader(
                                        key,
                                        values.stream()
                                                .filter(Objects::nonNull)
                                                .map(v -> v.trim())
                                                .toArray(String[]::new));
                            }
                        });
            }
            return TokenRequest.parse(request);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e); // todo check this error msg has request id
            throw new AccessTokenValidationException();
        }
    }

    public AccessTokenResponse createAndSaveAccessToken(TokenRequest tokenRequest)
            throws AccessTokenValidationException, AccessTokenProcessingException {
        ValidationResult<ErrorObject> validationResult = validateTokenRequest(tokenRequest);
        if (!validationResult.isValid()) {
            LOGGER.error(
                    "Invalid access token request, error description: {}",
                    validationResult.getError().getDescription());

            throw new AccessTokenValidationException();
        }

        try {
            TokenResponse tokenResponse = generateAccessToken(tokenRequest);
            AccessTokenResponse accessTokenResponse = tokenResponse.toSuccessResponse();
            persistAccessToken(accessTokenResponse, UUID.randomUUID().toString());
            return accessTokenResponse;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e); // todo check this error msg has request id
            throw new AccessTokenProcessingException();
        }
    }

    protected TokenResponse generateAccessToken(TokenRequest tokenRequest) {
        AccessToken accessToken =
                new BearerAccessToken(
                        configurationService.getBearerAccessTokenTtl(), tokenRequest.getScope());
        return new AccessTokenResponse(new Tokens(accessToken, null));
    }

    protected ValidationResult<ErrorObject> validateTokenRequest(TokenRequest tokenRequest) {
        if (!tokenRequest.getAuthorizationGrant().getType().equals(GrantType.CLIENT_CREDENTIALS)) {
            return new ValidationResult<>(false, OAuth2Error.UNSUPPORTED_GRANT_TYPE);
        }
        ClientSecretBasic clientAuthentication =
                (ClientSecretBasic) tokenRequest.getClientAuthentication();
        return ValidationResult.createValidResult();
    }

    public String getResourceIdByAccessToken(String accessToken) {
        AccessTokenItem accessTokenItem = dataStore.getItem(accessToken);
        return Objects.isNull(accessTokenItem) ? null : accessTokenItem.getResourceId();
    }

    protected void persistAccessToken(AccessTokenResponse tokenResponse, String resourceId) {
        AccessTokenItem accessTokenItem = new AccessTokenItem();
        accessTokenItem.setAccessToken(
                tokenResponse.getTokens().getBearerAccessToken().toAuthorizationHeader());
        accessTokenItem.setResourceId(resourceId);
        accessTokenItem.setExpiryDate(
                Instant.now()
                        .plus(configurationService.getBearerAccessTokenTtl(), ChronoUnit.SECONDS)
                        .getEpochSecond());
        dataStore.create(accessTokenItem);
    }
}
