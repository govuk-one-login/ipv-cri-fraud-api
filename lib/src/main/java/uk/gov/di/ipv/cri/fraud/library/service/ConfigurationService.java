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
        return ssmProvider.get(SSMParameterName.ACCESS_TOKEN_TABLE_NAME.getValue());
    }

    public long getBearerAccessTokenTtl() {
        return Optional.ofNullable(
                        ssmProvider.get(SSMParameterName.BEARER_ACCESS_TOKEN_TTL.getValue()))
                .map(Long::valueOf)
                .orElse(DEFAULT_BEARER_TOKEN_TTL_IN_SECS);
    }

    public enum SSMParameterName {
        ACCESS_TOKEN_TABLE_NAME("AccessTokenTableName"),
        BEARER_ACCESS_TOKEN_TTL("BearerAccessTokenTtl");

        private String value;

        SSMParameterName(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
}
