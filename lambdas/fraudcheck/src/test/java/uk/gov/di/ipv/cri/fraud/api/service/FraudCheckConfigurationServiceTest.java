package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.lambda.powertools.parameters.ParamProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.amazon.lambda.powertools.parameters.transform.Transformer.json;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class FraudCheckConfigurationServiceTest {
    private static final String KEY_FORMAT = "/%s/credentialIssuers/fraud/%s";

    private static final String STACK_PARAMETER_FORMAT = "/%s/%s";

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private final String ENVIRONMENT = "dev"; // env used for older ccv1 secrets manager parameters
    private final String AWS_STACK_NAME = "fraud-api-dev";
    private final String PARAMETER_PREFIX = AWS_STACK_NAME;
    private final String COMMON_PARAMETER_NAME_PREFIX = "common-cri-api";

    @Mock private SecretsProvider mockSecretsProvider;
    @Mock private ParamProvider mockParamProvider;

    @Test
    void shouldInitialiseConfigFieldsWhenValidInputProvided() {
        environmentVariables.set("ENVIRONMENT", ENVIRONMENT);
        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("PARAMETER_PREFIX", PARAMETER_PREFIX);
        environmentVariables.set("AWS_STACK_NAME", AWS_STACK_NAME);
        environmentVariables.set("COMMON_PARAMETER_NAME_PREFIX", COMMON_PARAMETER_NAME_PREFIX);

        String tenantIdKey = "thirdPartyApiTenantId";
        String tenantIdValue = "test-tenant-id";

        String hmacKey = "thirdPartyApiHmacKey";
        String testHmacKeyValue = "test-key";

        String endpointKey = "thirdPartyApiEndpointUrl";
        String endpointValue = "test-endpoint";

        String thirdPartyIdKey = "thirdPartyId";
        String thirdPartyIdValue = "third-party-id";

        String keyStoreKey = "thirdPartyApiKeyStore";

        String fraudTableNameKey = "FraudTableName";
        String fraudTableNameValue = "FraudTableValue";

        String contraindicationMappingsKey = "contraindicationMappings";
        String contraindicationMappingsValue = "null:null";

        String zeroScoreUcodesKey = "zeroScoreUcodes";
        String zeroScoreUcodesValue = "U001,U002";
        List<String> zeroScoreUcodesListValue = List.of(zeroScoreUcodesValue.split(","));

        String noFileFoundThresholdKey = "noFileFoundThreshold";
        Integer noFileFoundThresholdValue = 35;

        String sessionTtlKey = "SessionTtl";
        long sessionTtlValue = 7200L;

        String crosscoreV2Enabled = "CrosscoreV2/enabled";
        boolean crosscoreV2EnabledValue = false;

        FraudCheckConfigurationService.KeyStoreParams testKeyStoreParams =
                new FraudCheckConfigurationService.KeyStoreParams();
        testKeyStoreParams.setKeyStore("keystore");
        testKeyStoreParams.setKeyStorePassword("pwd");

        when(mockParamProvider.get(String.format(KEY_FORMAT, ENVIRONMENT, tenantIdKey)))
                .thenReturn(tenantIdValue);
        when(mockParamProvider.get(String.format(KEY_FORMAT, ENVIRONMENT, endpointKey)))
                .thenReturn(endpointValue);
        when(mockParamProvider.get(String.format(KEY_FORMAT, ENVIRONMENT, thirdPartyIdKey)))
                .thenReturn(thirdPartyIdValue);

        when(mockParamProvider.get(
                        String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, fraudTableNameKey)))
                .thenReturn(fraudTableNameValue);
        when(mockParamProvider.get(
                        String.format(
                                STACK_PARAMETER_FORMAT,
                                AWS_STACK_NAME,
                                contraindicationMappingsKey)))
                .thenReturn(contraindicationMappingsValue);
        when(mockParamProvider.get(
                        String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, zeroScoreUcodesKey)))
                .thenReturn(zeroScoreUcodesValue);
        when(mockParamProvider.get(
                        String.format(
                                STACK_PARAMETER_FORMAT, AWS_STACK_NAME, noFileFoundThresholdKey)))
                .thenReturn(String.valueOf(noFileFoundThresholdValue));
        when(mockParamProvider.get(
                        String.format(
                                STACK_PARAMETER_FORMAT,
                                COMMON_PARAMETER_NAME_PREFIX,
                                sessionTtlKey)))
                .thenReturn(String.valueOf(sessionTtlValue));

        when(mockParamProvider.get(
                        String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, crosscoreV2Enabled)))
                .thenReturn(String.valueOf(crosscoreV2EnabledValue));

        when(mockSecretsProvider.get(String.format(KEY_FORMAT, ENVIRONMENT, hmacKey)))
                .thenReturn(testHmacKeyValue);
        when(mockSecretsProvider.get(
                        String.format(KEY_FORMAT, ENVIRONMENT, keyStoreKey),
                        FraudCheckConfigurationService.KeyStoreParams.class))
                .thenReturn(testKeyStoreParams);
        when(mockSecretsProvider.withTransformation(json)).thenReturn(mockSecretsProvider);

        FraudCheckConfigurationService fraudCheckConfigurationService =
                new FraudCheckConfigurationService(
                        mockSecretsProvider, mockParamProvider, ENVIRONMENT);

        assertNotNull(fraudCheckConfigurationService);
        verify(mockParamProvider).get(String.format(KEY_FORMAT, ENVIRONMENT, tenantIdKey));
        verify(mockParamProvider).get(String.format(KEY_FORMAT, ENVIRONMENT, endpointKey));
        verify(mockParamProvider).get(String.format(KEY_FORMAT, ENVIRONMENT, thirdPartyIdKey));
        verify(mockSecretsProvider).get(String.format(KEY_FORMAT, ENVIRONMENT, hmacKey));

        verify(mockParamProvider)
                .get(String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, fraudTableNameKey));
        verify(mockParamProvider)
                .get(
                        String.format(
                                STACK_PARAMETER_FORMAT,
                                AWS_STACK_NAME,
                                contraindicationMappingsKey));
        verify(mockParamProvider)
                .get(String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, zeroScoreUcodesKey));
        verify(mockParamProvider)
                .get(
                        String.format(
                                STACK_PARAMETER_FORMAT, AWS_STACK_NAME, noFileFoundThresholdKey));
        verify(mockParamProvider)
                .get(
                        String.format(
                                STACK_PARAMETER_FORMAT,
                                COMMON_PARAMETER_NAME_PREFIX,
                                sessionTtlKey));

        verify(mockParamProvider)
                .get(String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, crosscoreV2Enabled));

        verify(mockSecretsProvider)
                .get(
                        String.format(KEY_FORMAT, ENVIRONMENT, keyStoreKey),
                        FraudCheckConfigurationService.KeyStoreParams.class);
        assertEquals(
                testKeyStoreParams.getKeyStore(),
                fraudCheckConfigurationService.getEncodedKeyStore());
        assertEquals(
                testKeyStoreParams.getKeyStorePassword(),
                fraudCheckConfigurationService.getKeyStorePassword());
        assertEquals(testHmacKeyValue, fraudCheckConfigurationService.getHmacKey());
        assertEquals(thirdPartyIdValue, fraudCheckConfigurationService.getThirdPartyId());
        assertEquals(endpointValue, fraudCheckConfigurationService.getEndpointUrl());
        assertEquals(tenantIdValue, fraudCheckConfigurationService.getTenantId());

        assertEquals(fraudTableNameValue, fraudCheckConfigurationService.getFraudResultTableName());
        assertEquals(
                contraindicationMappingsValue,
                fraudCheckConfigurationService.getContraindicationMappings());
        assertEquals(zeroScoreUcodesListValue, fraudCheckConfigurationService.getZeroScoreUcodes());
        assertEquals(
                noFileFoundThresholdValue,
                fraudCheckConfigurationService.getNoFileFoundThreshold());
        assertEquals(sessionTtlValue, fraudCheckConfigurationService.getFraudResultItemTtl());

        assertEquals(crosscoreV2EnabledValue, fraudCheckConfigurationService.crosscoreV2Enabled());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenSecretsProviderNull() {
        NullPointerException thrownException =
                assertThrows(
                        NullPointerException.class,
                        () -> new FraudCheckConfigurationService(null, null, ""));
        assertEquals("secretsProvider must not be null", thrownException.getMessage());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenParamProviderNull() {
        NullPointerException thrownException =
                assertThrows(
                        NullPointerException.class,
                        () -> new FraudCheckConfigurationService(mockSecretsProvider, null, ""));
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
                                                    new FraudCheckConfigurationService(
                                                            mockSecretsProvider,
                                                            mockParamProvider,
                                                            env));
                            assertEquals("env must be specified", thrownException.getMessage());
                        });
    }
}
