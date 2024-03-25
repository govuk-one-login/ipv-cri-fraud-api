package uk.gov.di.ipv.cri.fraud.api.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import software.amazon.lambda.powertools.parameters.ParamProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FraudCheckConfigurationService {
    private static final String PARAMETER_NAME_FORMAT = "/%s/%s";

    private final String fraudResultTableName;
    private final String contraindicationMappings;
    private final String parameterPrefix;
    private final String stackParameterPrefix;
    private final String commonParameterPrefix;

    private final boolean pepEnabled;

    private List<String> zeroScoreUcodes;
    private Integer noFileFoundThreshold;
    private final long fraudResultItemTtl;

    private final CrosscoreV2Configuration crosscoreV2Configuration;

    private final Clock clock;

    public FraudCheckConfigurationService(
            SecretsProvider secretsProvider, ParamProvider paramProvider, String env) {
        this.parameterPrefix =
                Optional.ofNullable(System.getenv("PARAMETER_PREFIX"))
                        .orElse(System.getenv("AWS_STACK_NAME"));
        this.stackParameterPrefix = System.getenv("AWS_STACK_NAME");
        this.commonParameterPrefix = System.getenv("COMMON_PARAMETER_NAME_PREFIX");

        Objects.requireNonNull(secretsProvider, "secretsProvider must not be null");
        Objects.requireNonNull(paramProvider, "paramProvider must not be null");

        if (StringUtils.isBlank(env)) {
            throw new IllegalArgumentException("env must be specified");
        }

        // ****************************Private Parameters****************************

        this.clock = Clock.systemUTC();

        this.contraindicationMappings =
                paramProvider.get(getParameterName("contraindicationMappings"));
        this.fraudResultTableName = paramProvider.get(getStackParameterName("FraudTableName"));
        this.zeroScoreUcodes =
                Arrays.asList(paramProvider.get(getParameterName("zeroScoreUcodes")).split(","));
        this.noFileFoundThreshold =
                Integer.valueOf(paramProvider.get(getParameterName("noFileFoundThreshold")));
        this.fraudResultItemTtl =
                Long.parseLong(paramProvider.get(getCommonParameterName("SessionTtl")));

        // *************************CrosscoreV2 Parameters***************************

        this.crosscoreV2Configuration =
                new CrosscoreV2Configuration(paramProvider, parameterPrefix, stackParameterPrefix);

        // *****************************Feature Toggles*******************************

        this.pepEnabled = Boolean.valueOf(paramProvider.get(getStackParameterName("pepEnabled")));
    }

    public String getFraudResultTableName() {
        return fraudResultTableName;
    }

    public String getContraindicationMappings() {
        return contraindicationMappings;
    }

    public CrosscoreV2Configuration getCrosscoreV2Configuration() {
        return crosscoreV2Configuration;
    }

    public boolean getPepEnabled() {
        return pepEnabled;
    }

    public List<String> getZeroScoreUcodes() {
        return zeroScoreUcodes;
    }

    public long getFraudResultItemTtl() {
        return fraudResultItemTtl;
    }

    public Integer getNoFileFoundThreshold() {
        return noFileFoundThreshold;
    }

    public long getFraudResultItemExpirationEpoch() {
        return clock.instant().plus(fraudResultItemTtl, ChronoUnit.SECONDS).getEpochSecond();
    }

    private String getParameterName(String parameterName) {
        return String.format(PARAMETER_NAME_FORMAT, parameterPrefix, parameterName);
    }

    private String getStackParameterName(String parameterName) {
        return String.format(PARAMETER_NAME_FORMAT, stackParameterPrefix, parameterName);
    }

    private String getCommonParameterName(String parameterName) {
        return String.format(PARAMETER_NAME_FORMAT, commonParameterPrefix, parameterName);
    }
}
