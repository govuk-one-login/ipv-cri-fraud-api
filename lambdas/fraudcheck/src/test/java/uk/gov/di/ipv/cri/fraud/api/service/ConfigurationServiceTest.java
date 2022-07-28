package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.lambda.powertools.parameters.ParamProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.amazon.lambda.powertools.parameters.transform.Transformer.json;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {
    private static final String KEY_FORMAT = "/%s/credentialIssuers/fraud/%s";
    @Mock private SecretsProvider mockSecretsProvider;
    @Mock private ParamProvider mockParamProvider;

    @Test
    void shouldInitialiseConfigFieldsWhenValidInputProvided() {
        String env = "dev";

        String tenantIdKey = "thirdPartyApiTenantId";
        String tenantIdValue = "test-tenant-id";
        String hmacKey = "thirdPartyApiHmacKey";
        String testHmacKeyValue = "test-key";
        String endpointKey = "thirdPartyApiEndpointUrl";
        String endpointValue = "test-endpoint";
        String thirdPartyIdKey = "thirdPartyId";
        String thirdPartyIdValue = "third-party-id";
        String keyStoreKey = "thirdPartyApiKeyStore";

        ConfigurationService.KeyStoreParams testKeyStoreParams =
                new ConfigurationService.KeyStoreParams();
        testKeyStoreParams.setKeyStore("keystore");
        testKeyStoreParams.setKeyStorePassword("pwd");

        when(mockParamProvider.get(String.format(KEY_FORMAT, env, tenantIdKey)))
                .thenReturn(tenantIdValue);
        when(mockParamProvider.get(String.format(KEY_FORMAT, env, endpointKey)))
                .thenReturn(endpointValue);
        when(mockParamProvider.get(String.format(KEY_FORMAT, env, thirdPartyIdKey)))
                .thenReturn(thirdPartyIdValue);

        when(mockSecretsProvider.get(String.format(KEY_FORMAT, env, hmacKey)))
                .thenReturn(testHmacKeyValue);
        when(mockSecretsProvider.get(
                        String.format(KEY_FORMAT, env, keyStoreKey),
                        ConfigurationService.KeyStoreParams.class))
                .thenReturn(testKeyStoreParams);
        when(mockSecretsProvider.withTransformation(json)).thenReturn(mockSecretsProvider);

        ConfigurationService configurationService =
                new ConfigurationService(mockSecretsProvider, mockParamProvider, env);

        assertNotNull(configurationService);
        verify(mockParamProvider).get(String.format(KEY_FORMAT, env, tenantIdKey));
        verify(mockParamProvider).get(String.format(KEY_FORMAT, env, endpointKey));
        verify(mockParamProvider).get(String.format(KEY_FORMAT, env, thirdPartyIdKey));
        verify(mockSecretsProvider).get(String.format(KEY_FORMAT, env, hmacKey));
        verify(mockSecretsProvider)
                .get(
                        String.format(KEY_FORMAT, env, keyStoreKey),
                        ConfigurationService.KeyStoreParams.class);
        assertEquals(testKeyStoreParams.getKeyStore(), configurationService.getEncodedKeyStore());
        assertEquals(
                testKeyStoreParams.getKeyStorePassword(),
                configurationService.getKeyStorePassword());
        assertEquals(testHmacKeyValue, configurationService.getHmacKey());
        assertEquals(thirdPartyIdValue, configurationService.getThirdPartyId());
        assertEquals(endpointValue, configurationService.getEndpointUrl());
        assertEquals(tenantIdValue, configurationService.getTenantId());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenSecretsProviderNull() {
        NullPointerException thrownException =
                assertThrows(
                        NullPointerException.class, () -> new ConfigurationService(null, null, ""));
        assertEquals("secretsProvider must not be null", thrownException.getMessage());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenParamProviderNull() {
        NullPointerException thrownException =
                assertThrows(
                        NullPointerException.class,
                        () -> new ConfigurationService(mockSecretsProvider, null, ""));
        assertEquals("paramProvider must not be null", thrownException.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenEnvNullOrEmpty() {
        Arrays.stream(new String[] {null, "", "  "})
                .forEach(
                        (env) -> {
                            IllegalArgumentException thrownException =
                                    assertThrows(
                                            IllegalArgumentException.class,
                                            () ->
                                                    new ConfigurationService(
                                                            mockSecretsProvider,
                                                            mockParamProvider,
                                                            env));
                            assertEquals("env must be specified", thrownException.getMessage());
                        });
    }
}
