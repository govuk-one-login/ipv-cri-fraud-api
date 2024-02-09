package uk.gov.di.ipv.cri.fraud.api.service;

import software.amazon.lambda.powertools.parameters.ParamProvider;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

import java.net.URI;

@ExcludeFromGeneratedCoverageReport
public class CrosscoreV2Configuration {

    private final String parameterPrefix;
    private final String stackParameterPrefix;
    private final String tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;
    private final String tokenTableName;
    private final String userDomain;
    private final URI endpointUri;
    private final String tenantId;
    private final String tokenIssuer;

    public CrosscoreV2Configuration(
            ParamProvider paramProvider, String parameterPrefix, String stackParameterPrefix) {
        this.parameterPrefix = parameterPrefix;
        this.stackParameterPrefix = stackParameterPrefix;

        this.tokenEndpoint = paramProvider.get(getParameterName("CrosscoreV2/tokenEndpoint"));
        this.clientId = paramProvider.get(getParameterName("CrosscoreV2/clientId"));
        this.clientSecret = paramProvider.get(getParameterName("CrosscoreV2/clientSecret"));
        this.username = paramProvider.get(getParameterName("CrosscoreV2/Username"));
        this.password = paramProvider.get(getParameterName("CrosscoreV2/Password"));
        this.userDomain = paramProvider.get(getParameterName("CrosscoreV2/userDomain"));

        this.tokenTableName =
                paramProvider.get(getStackParameterName("CrosscoreV2/tokenTableName"));
        this.endpointUri =
                URI.create(paramProvider.get(getParameterName("CrosscoreV2/endpointUrl")));
        this.tenantId = paramProvider.get(getParameterName("CrosscoreV2/tenantId"));
        this.tokenIssuer = paramProvider.get(getParameterName("CrosscoreV2/tokenIssuer"));
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUserDomain() {
        return userDomain;
    }

    public String getTokenTableName() {
        return tokenTableName;
    }

    public URI getEndpointUri() {
        return endpointUri;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getTokenIssuer() {
        return tokenIssuer;
    }

    private String getParameterName(String parameterName) {
        return String.format("/%s/%s", parameterPrefix, parameterName);
    }

    private String getStackParameterName(String parameterName) {
        return String.format("/%s/%s", stackParameterPrefix, parameterName);
    }
}
