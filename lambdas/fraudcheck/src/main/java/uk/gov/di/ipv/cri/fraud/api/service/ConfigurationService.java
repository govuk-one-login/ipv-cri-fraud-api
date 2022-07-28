package uk.gov.di.ipv.cri.fraud.api.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.parameters.ParamProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

import java.util.Objects;

import static software.amazon.lambda.powertools.parameters.transform.Transformer.json;

public class ConfigurationService {

    private static final Logger LOGGER = LogManager.getLogger();

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

    private final String tenantId;
    private final String endpointUrl;
    private final String hmacKey;
    private final String encodedKeyStore;
    private final String keyStorePassword;
    private final String thirdPartyId;
    private final String fraudResultTableName;
    private final String contraindicationMappings;
    private final String parameterPrefix;

    public ConfigurationService(
            SecretsProvider secretsProvider, ParamProvider paramProvider, String env) {
        Objects.requireNonNull(secretsProvider, "secretsProvider must not be null");
        Objects.requireNonNull(paramProvider, "paramProvider must not be null");

        if (StringUtils.isBlank(env)) {
            throw new IllegalArgumentException("env must be specified");
        }

        this.tenantId = paramProvider.get(String.format(KEY_FORMAT, env, "thirdPartyApiTenantId"));
        this.endpointUrl =
                paramProvider.get(String.format(KEY_FORMAT, env, "thirdPartyApiEndpointUrl"));
        this.hmacKey = secretsProvider.get(String.format(KEY_FORMAT, env, "thirdPartyApiHmacKey"));
        this.thirdPartyId = paramProvider.get(String.format(KEY_FORMAT, env, "thirdPartyId"));

        this.parameterPrefix = System.getenv("AWS_STACK_NAME");
        this.contraindicationMappings =
                paramProvider.get(getParameterName("contraindicationMappings"));
        this.fraudResultTableName = paramProvider.get(getParameterName("FraudTableName"));

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

    public String getParameterName(String parameterName) {
        return String.format("/%s/%s", parameterPrefix, parameterName);
    }
}
