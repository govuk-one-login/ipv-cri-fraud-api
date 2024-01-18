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

import static software.amazon.lambda.powertools.parameters.transform.Transformer.json;

public class FraudCheckConfigurationService {

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
    private final String stackParameterPrefix;
    private final String commonParameterPrefix;

    private final boolean crosscoreV2Enabled;
    private final boolean pepEnabled;

    private List<String> zeroScoreUcodes;
    private Integer noFileFoundThreshold;
    private final long fraudResultItemTtl;
    private final String tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;
    private final String tokenTableName;
    private final String userDomain;

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

        this.tenantId = paramProvider.get(String.format(KEY_FORMAT, env, "thirdPartyApiTenantId"));
        this.endpointUrl =
                paramProvider.get(String.format(KEY_FORMAT, env, "thirdPartyApiEndpointUrl"));
        this.hmacKey = secretsProvider.get(String.format(KEY_FORMAT, env, "thirdPartyApiHmacKey"));
        this.thirdPartyId = paramProvider.get(String.format(KEY_FORMAT, env, "thirdPartyId"));

        this.contraindicationMappings =
                paramProvider.get(getParameterName("contraindicationMappings"));
        this.fraudResultTableName = paramProvider.get(getStackParameterName("FraudTableName"));
        this.zeroScoreUcodes =
                Arrays.asList(paramProvider.get(getParameterName("zeroScoreUcodes")).split(","));
        this.noFileFoundThreshold =
                Integer.valueOf(paramProvider.get(getParameterName("noFileFoundThreshold")));
        this.fraudResultItemTtl =
                Long.parseLong(paramProvider.get(getCommonParameterName("SessionTtl")));

        // *************************Authenticate Parameters***************************

        this.tokenEndpoint = paramProvider.get(getParameterName("CrosscoreV2/tokenEndpoint"));
        this.clientId = paramProvider.get(getParameterName("CrosscoreV2/clientId"));
        this.clientSecret = paramProvider.get(getParameterName("CrosscoreV2/clientSecret"));
        this.username = paramProvider.get(getParameterName("CrosscoreV2/Username"));
        this.password = paramProvider.get(getParameterName("CrosscoreV2/Password"));
        this.userDomain = paramProvider.get(getParameterName("CrosscoreV2/userDomain"));

        this.tokenTableName =
                paramProvider.get(getStackParameterName("CrosscoreV2/tokenTableName"));

        // *****************************Feature Toggles*******************************

        this.crosscoreV2Enabled =
                Boolean.valueOf(paramProvider.get(getStackParameterName("CrosscoreV2/enabled")));

        this.pepEnabled = Boolean.valueOf(paramProvider.get(getStackParameterName("pepEnabled")));

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

    public boolean crosscoreV2Enabled() {
        return crosscoreV2Enabled;
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

    public long getFraudResultItemExpirationEpoch() {
        return clock.instant().plus(fraudResultItemTtl, ChronoUnit.SECONDS).getEpochSecond();
    }

    private String getParameterName(String parameterName) {
        return String.format("/%s/%s", parameterPrefix, parameterName);
    }

    private String getStackParameterName(String parameterName) {
        return String.format("/%s/%s", stackParameterPrefix, parameterName);
    }

    private String getCommonParameterName(String parameterName) {
        return String.format(PARAMETER_NAME_FORMAT, commonParameterPrefix, parameterName);
    }
}
