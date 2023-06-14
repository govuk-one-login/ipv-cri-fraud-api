package uk.gov.di.ipv.cri.fraud.api.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.parameters.ParamProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

import java.util.Objects;

public class ConfigurationService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final String fraudResultTableName;
    private final String contraindicationMappings;
    private final boolean activityHistoryEnabled;
    private final String parameterPrefix;

    public ConfigurationService(
            SecretsProvider secretsProvider, ParamProvider paramProvider, String env) {
        Objects.requireNonNull(secretsProvider, "secretsProvider must not be null");
        Objects.requireNonNull(paramProvider, "paramProvider must not be null");
        if (StringUtils.isBlank(env)) {
            throw new IllegalArgumentException("env must be specified");
        }

        this.parameterPrefix = System.getenv("AWS_STACK_NAME");
        this.contraindicationMappings =
                paramProvider.get(getParameterName("contraindicationMappings"));
        this.fraudResultTableName = paramProvider.get(getParameterName("FraudTableName"));
        this.activityHistoryEnabled =
                Boolean.parseBoolean(paramProvider.get(getParameterName("activityHistoryEnabled")));
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

    public String getParameterName(String parameterName) {
        return String.format("/%s/%s", parameterPrefix, parameterName);
    }
}
