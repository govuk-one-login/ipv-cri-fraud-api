package uk.gov.di.ipv.cri.fraud.api.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.parameters.ParamProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

import java.util.Objects;
import java.util.Optional;

public class ConfigurationService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final String fraudResultTableName;
    private final String contraindicationMappings;
    private final boolean activityHistoryEnabled;
    private final String parameterPrefix;
    private final String stackParameterPrefix;

    public ConfigurationService(
            SecretsProvider secretsProvider, ParamProvider paramProvider, String env) {
        Objects.requireNonNull(secretsProvider, "secretsProvider must not be null");
        Objects.requireNonNull(paramProvider, "paramProvider must not be null");
        if (StringUtils.isBlank(env)) {
            throw new IllegalArgumentException("env must be specified");
        }

        this.parameterPrefix =
                Optional.ofNullable(System.getenv("PARAMETER_PREFIX"))
                        .orElse(System.getenv("AWS_STACK_NAME"));
        this.stackParameterPrefix = System.getenv("AWS_STACK_NAME");

        this.contraindicationMappings =
                paramProvider.get(getParameterName("contraindicationMappings"));
        this.fraudResultTableName = paramProvider.get(getStackParameterName("FraudTableName"));
        this.activityHistoryEnabled =
                Boolean.parseBoolean(
                        paramProvider.get(getStackParameterName("activityHistoryEnabled")));
    }

    public String getFraudResultTableName() {
        return fraudResultTableName;
    }

    public String getContraindicationMappings() {
        return contraindicationMappings;
    }

    public boolean isActivityHistoryEnabled() {
        return activityHistoryEnabled;
    }

    private String getParameterName(String parameterName) {
        return String.format("/%s/%s", parameterPrefix, parameterName);
    }

    private String getStackParameterName(String parameterName) {
        return String.format("/%s/%s", stackParameterPrefix, parameterName);
    }
}
