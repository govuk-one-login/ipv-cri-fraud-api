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

        String experianTokenTableNameKey = "CrosscoreV2/tokenTableName";
        String experianTokenTableValue = "ExperianTokenTableValue";

        String tokenEndpointKey = "CrosscoreV2/tokenEndpoint";
        String tokenEndpointValue = "tokenEndpointValue";

        String tokenClientIdKey = "CrosscoreV2/clientId";
        String tokenClientIdValue = "clientIdValue";

        String tokenClientSecretKey = "CrosscoreV2/clientSecret";
        String tokenClientSecretValue = "clientSecretValue";

        String tokenUserNameKey = "CrosscoreV2/Username";
        String tokenUserNameValue = "tokenUserValue";

        String tokenPasswordKey = "CrosscoreV2/Password";
        String tokenPasswordValue = "tokenPasswordValue";

        String tokenUserDomainKey = "CrosscoreV2/userDomain";
        String tokenUserDomainValue = "tokenUserDomainValue";

        String crosscoreV2EndpointUrlKey = "CrosscoreV2/endpointUrl";
        String crosscoreV2EndpointUrlValue = "crosscoreV2EndpointUrlValue";

        String crosscoreV2TenantIdKey = "CrosscoreV2/tenantId";
        String crosscoreV2TenantIdValue = "crosscoreV2TenantId";

        String crosscoreV2TokenIssuerKey = "CrosscoreV2/tokenIssuer";
        String crosscoreV2TokenIssuerValue = "crosscoreV2TokenIssuer";

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

        // **********************************CrossCoreV2Params*********************************************

        when(mockParamProvider.get(
                        String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenEndpointKey)))
                .thenReturn(tokenEndpointValue);

        when(mockParamProvider.get(
                        String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenClientIdKey)))
                .thenReturn(tokenClientIdValue);

        when(mockParamProvider.get(
                        String.format(
                                STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenClientSecretKey)))
                .thenReturn(tokenClientSecretValue);

        when(mockParamProvider.get(
                        String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenUserNameKey)))
                .thenReturn(tokenUserNameValue);

        when(mockParamProvider.get(
                        String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenPasswordKey)))
                .thenReturn(tokenPasswordValue);

        when(mockParamProvider.get(
                        String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenUserDomainKey)))
                .thenReturn(tokenUserDomainValue);

        when(mockParamProvider.get(
                        String.format(
                                STACK_PARAMETER_FORMAT, AWS_STACK_NAME, experianTokenTableNameKey)))
                .thenReturn(experianTokenTableValue);

        when(mockParamProvider.get(
                        String.format(
                                STACK_PARAMETER_FORMAT, AWS_STACK_NAME, crosscoreV2EndpointUrlKey)))
                .thenReturn(crosscoreV2EndpointUrlValue);

        when(mockParamProvider.get(
                        String.format(
                                STACK_PARAMETER_FORMAT, AWS_STACK_NAME, crosscoreV2TenantIdKey)))
                .thenReturn(crosscoreV2TenantIdValue);

        when(mockParamProvider.get(
                        String.format(
                                STACK_PARAMETER_FORMAT, AWS_STACK_NAME, crosscoreV2TokenIssuerKey)))
                .thenReturn(crosscoreV2TokenIssuerValue);

        // ***************************************************************************************************

        FraudCheckConfigurationService fraudCheckConfigurationService =
                new FraudCheckConfigurationService(
                        mockSecretsProvider, mockParamProvider, ENVIRONMENT);

        assertNotNull(fraudCheckConfigurationService);

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
        // **********************************CrossCoreV2
        // Params*********************************************

        verify(mockParamProvider)
                .get(String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenEndpointKey));
        verify(mockParamProvider)
                .get(String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenClientIdKey));
        verify(mockParamProvider)
                .get(String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenClientSecretKey));
        verify(mockParamProvider)
                .get(String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenUserNameKey));
        verify(mockParamProvider)
                .get(String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenPasswordKey));
        verify(mockParamProvider)
                .get(String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, tokenUserDomainKey));
        verify(mockParamProvider)
                .get(String.format(STACK_PARAMETER_FORMAT, AWS_STACK_NAME, crosscoreV2TenantIdKey));
        verify(mockParamProvider)
                .get(
                        String.format(
                                STACK_PARAMETER_FORMAT, AWS_STACK_NAME, crosscoreV2TokenIssuerKey));
        verify(mockParamProvider)
                .get(
                        String.format(
                                STACK_PARAMETER_FORMAT, AWS_STACK_NAME, experianTokenTableNameKey));

        // ***************************************************************************************************

        assertEquals(fraudTableNameValue, fraudCheckConfigurationService.getFraudResultTableName());
        assertEquals(
                contraindicationMappingsValue,
                fraudCheckConfigurationService.getContraindicationMappings());
        assertEquals(zeroScoreUcodesListValue, fraudCheckConfigurationService.getZeroScoreUcodes());
        assertEquals(
                noFileFoundThresholdValue,
                fraudCheckConfigurationService.getNoFileFoundThreshold());
        assertEquals(sessionTtlValue, fraudCheckConfigurationService.getFraudResultItemTtl());

        // **********************************CrossCoreV2
        // Params*********************************************

        assertEquals(
                tokenEndpointValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getTokenEndpoint());
        assertEquals(
                tokenClientIdValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getClientId());
        assertEquals(
                tokenClientSecretValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getClientSecret());
        assertEquals(
                tokenUserNameValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getUsername());
        assertEquals(
                tokenPasswordValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getPassword());
        assertEquals(
                tokenUserDomainValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getUserDomain());
        assertEquals(
                crosscoreV2TenantIdValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getTenantId());
        assertEquals(
                crosscoreV2TokenIssuerValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getTokenIssuer());
        assertEquals(
                experianTokenTableValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getTokenTableName());
        // ***************************************************************************************************

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
