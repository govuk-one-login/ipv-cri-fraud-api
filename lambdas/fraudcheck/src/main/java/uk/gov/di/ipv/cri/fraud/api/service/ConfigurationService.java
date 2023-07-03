package uk.gov.di.ipv.cri.fraud.api.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import software.amazon.lambda.powertools.parameters.ParamProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static software.amazon.lambda.powertools.parameters.transform.Transformer.json;

public class ConfigurationService {

    static class KeyStoreParams {
        private String keyStore;
        private String keyStorePassword;

        public String getKeyStore() {
            return keyStore;
        }

        public void setKeyStore(String keyStore) {
            this.keyStore = keyStore;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public void setKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
        }
    }

    private static final String KEY_FORMAT = "/%s/credentialIssuers/fraud/%s";
    private static final String PARAMETER_NAME_FORMAT = "/%s/%s";

    private final String tenantId;
    private final String endpointUrl;
    private final String hmacKey;
    private final String encodedKeyStore;
    private final String keyStorePassword;
    private final String thirdPartyId;
    private final String fraudResultTableName;
    private final String contraindicationMappings;
    private final String parameterPrefix;
    private final String commonParameterPrefix;
    private final boolean pepEnabled;
    private List<String> zeroScoreUcodes;
    private Integer noFileFoundThreshold;
    private final long fraudResultItemTtl;

    private final Clock clock;

    public ConfigurationService(
            SecretsProvider secretsProvider, ParamProvider paramProvider, String env) {
        Objects.requireNonNull(secretsProvider, "secretsProvider must not be null");
        Objects.requireNonNull(paramProvider, "paramProvider must not be null");

        if (StringUtils.isBlank(env)) {
            throw new IllegalArgumentException("env must be specified");
        }

        // ****************************Private Parameters****************************

        this.parameterPrefix = System.getenv("AWS_STACK_NAME");
        this.commonParameterPrefix = System.getenv("COMMON_PARAMETER_NAME_PREFIX");
        this.clock = Clock.systemUTC();

        this.tenantId = paramProvider.get(String.format(KEY_FORMAT, env, "thirdPartyApiTenantId"));
        this.endpointUrl =
                paramProvider.get(String.format(KEY_FORMAT, env, "thirdPartyApiEndpointUrl"));
        this.hmacKey = secretsProvider.get(String.format(KEY_FORMAT, env, "thirdPartyApiHmacKey"));
        this.thirdPartyId = paramProvider.get(String.format(KEY_FORMAT, env, "thirdPartyId"));

        this.contraindicationMappings =
                paramProvider.get(getParameterName("contraindicationMappings"));
        this.fraudResultTableName = paramProvider.get(getParameterName("FraudTableName"));
        this.zeroScoreUcodes =
                Arrays.asList(paramProvider.get(getParameterName("zeroScoreUcodes")).split(","));
        this.noFileFoundThreshold =
                Integer.valueOf(paramProvider.get(getParameterName("noFileFoundThreshold")));
        this.fraudResultItemTtl =
                Long.parseLong(paramProvider.get(getCommonParameterName("SessionTtl")));

        // *****************************Feature Toggles*******************************

        this.pepEnabled = Boolean.valueOf(paramProvider.get(getParameterName("pepEnabled")));

        // *********************************Secrets***********************************

        KeyStoreParams keyStoreParams =
                secretsProvider
                        .withTransformation(json)
                        .get(
                                String.format(KEY_FORMAT, env, "thirdPartyApiKeyStore"),
                                KeyStoreParams.class);

        this.encodedKeyStore = keyStoreParams.getKeyStore();
        this.keyStorePassword = keyStoreParams.getKeyStorePassword();
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public String getHmacKey() {
        return hmacKey;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getEncodedKeyStore() {
        return encodedKeyStore;
    }

    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public String getFraudResultTableName() {
        return fraudResultTableName;
    }

    public String getContraindicationMappings() {
        return contraindicationMappings;
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

    public void setZeroScoreUcodes(List<String> zeroScoreUcodes) {
        this.zeroScoreUcodes = zeroScoreUcodes;
    }

    public Integer getNoFileFoundThreshold() {
        return noFileFoundThreshold;
    }

    public void setNoFileFoundThreshold(Integer noFileFoundThreshold) {
        this.noFileFoundThreshold = noFileFoundThreshold;
    }

    public long getFraudResultItemExpirationEpoch() {
        return clock.instant().plus(fraudResultItemTtl, ChronoUnit.SECONDS).getEpochSecond();
    }

    public String getParameterName(String parameterName) {
        return String.format("/%s/%s", parameterPrefix, parameterName);
    }

    private String getCommonParameterName(String parameterName) {
        return String.format(PARAMETER_NAME_FORMAT, commonParameterPrefix, parameterName);
    }
}
