package uk.gov.di.ipv.cri.fraud.library.service;

import software.amazon.lambda.powertools.parameters.ParamManager;
import software.amazon.lambda.powertools.parameters.SSMProvider;

import java.util.Optional;

public class ConfigurationService {

    private static final long DEFAULT_BEARER_TOKEN_TTL_IN_SECS = 3600L;
    private final SSMProvider ssmProvider;

    public ConfigurationService(SSMProvider ssmProvider) {
        this.ssmProvider = ssmProvider;
    }

    public ConfigurationService() {
        this.ssmProvider = ParamManager.getSsmProvider();
    }

    public String getAccessTokenTableName() {
        return ssmProvider.get(SSMParameterName.AccessTokenTableName.toString());
    }

    public long getBearerAccessTokenTtl() {
        return Optional.ofNullable(ssmProvider.get(SSMParameterName.BearerAccessTokenTtl.name()))
                .map(Long::valueOf)
                .orElse(DEFAULT_BEARER_TOKEN_TTL_IN_SECS);
    }

    public enum SSMParameterName {
        AccessTokenTableName,
        BearerAccessTokenTtl
    }
}
