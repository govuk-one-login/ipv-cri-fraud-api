package uk.gov.di.ipv.cri.fraud.api.service;

import lombok.Getter;
import uk.gov.di.ipv.cri.fraud.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;

import java.net.URI;
import java.util.Map;

@Getter
public class CrosscoreV2Configuration {

    public static final String CC2_PARAMETER_PATH = "CrosscoreV2";

    public static final String TOKEN_TABLE_NAME_PARAMETER_KEY = "CrosscoreV2/tokenTableName";
    public static final String TOKEN_END_POINT_PARAMETER_KEY = "tokenEndpoint";
    public static final String TOKEN_USER_DOMAIN_PARAMETER_KEY = "userDomain";
    public static final String TOKEN_USERNAME_PARAMETER_KEY = "Username";
    public static final String TOKEN_PASSWORD_PARAMETER_KEY = "Password";
    public static final String TOKEN_CLIENT_ID_PARAMETER_KEY = "clientId";
    public static final String TOKEN_CLIENT_SECRET_PARAMETER_KEY = "clientSecret";

    public static final String TOKEN_ISSUER_PARAMETER_KEY = "tokenIssuer";

    public static final String CC2_ENDPOINT_PARAMETER_KEY = "endpointUrl";
    public static final String CC2_TENANT_ID_PARAMETER_KEY = "tenantId";

    // Dynamo
    private final String tokenTableName;

    // Token Endpoint
    private final String tokenEndpoint;

    // Token Header
    private final String userDomain;

    // Token Body
    private final String username;
    private final String password;
    private final String clientId;
    private final String clientSecret;

    // Token Response Validation
    private final String tokenIssuer;

    // Crosscore Endpoint
    private final URI endpointUri;

    // Crosscore Header
    private final String tenantId;

    public CrosscoreV2Configuration(ParameterStoreService parameterStoreService) {

        Map<String, String> ccV2ParameterMap =
                parameterStoreService.getAllParametersFromPath(
                        ParameterPrefix.OVERRIDE, CC2_PARAMETER_PATH);

        // The map above contains a token table name but from the prefix.
        // The one used must always be the stacks token table name
        this.tokenTableName =
                parameterStoreService.getParameterValue(
                        ParameterPrefix.STACK, TOKEN_TABLE_NAME_PARAMETER_KEY);

        this.tokenEndpoint = ccV2ParameterMap.get(TOKEN_END_POINT_PARAMETER_KEY);

        this.userDomain = ccV2ParameterMap.get(TOKEN_USER_DOMAIN_PARAMETER_KEY);

        this.username = ccV2ParameterMap.get(TOKEN_USERNAME_PARAMETER_KEY);
        this.password = ccV2ParameterMap.get(TOKEN_PASSWORD_PARAMETER_KEY);
        this.clientId = ccV2ParameterMap.get(TOKEN_CLIENT_ID_PARAMETER_KEY);
        this.clientSecret = ccV2ParameterMap.get(TOKEN_CLIENT_SECRET_PARAMETER_KEY);

        this.tokenIssuer = ccV2ParameterMap.get(TOKEN_ISSUER_PARAMETER_KEY);

        this.endpointUri = URI.create(ccV2ParameterMap.get(CC2_ENDPOINT_PARAMETER_KEY));
        this.tenantId = ccV2ParameterMap.get(CC2_TENANT_ID_PARAMETER_KEY);
    }
}
