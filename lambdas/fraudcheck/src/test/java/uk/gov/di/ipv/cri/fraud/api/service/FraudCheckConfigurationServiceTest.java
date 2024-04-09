package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;
import uk.gov.di.ipv.cri.fraud.library.strategy.Strategy;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class FraudCheckConfigurationServiceTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private final String ENVIRONMENT = "dev"; // env used for older ccv1 secrets manager parameters
    private final String AWS_STACK_NAME = "fraud-api-dev";
    private final String AWS_REGION = "eu-west-2";
    private final String PARAMETER_PREFIX = "fraud-api-pipeline";
    private final String COMMON_PARAMETER_NAME_PREFIX = "common-cri-api";

    @Mock private ParameterStoreService mockParameterStoreService;

    @Test
    void shouldInitialiseConfigFieldsWhenValidInputProvided() throws JsonProcessingException {
        environmentVariables.set("ENVIRONMENT", ENVIRONMENT);
        environmentVariables.set("AWS_REGION", AWS_REGION);
        environmentVariables.set("PARAMETER_PREFIX", PARAMETER_PREFIX);
        environmentVariables.set("AWS_STACK_NAME", AWS_STACK_NAME);
        environmentVariables.set("COMMON_PARAMETER_NAME_PREFIX", COMMON_PARAMETER_NAME_PREFIX);

        // CRI
        String contraindicationMappingsValue = "null:null";
        String zeroScoreUcodesValue = "U001,U002";
        List<String> zeroScoreUcodesListValue = List.of(zeroScoreUcodesValue.split(","));
        Integer noFileFoundThresholdValue = 35;

        // CC2
        String experianTokenTableValue = "ExperianTokenTableValue";
        String tokenEndpointValue = "tokenEndpointValue";
        String tokenClientIdValue = "clientIdValue";
        String tokenClientSecretValue = "clientSecretValue";
        String tokenUserNameValue = "tokenUserValue";
        String tokenPasswordValue = "tokenPasswordValue";
        String tokenUserDomainValue = "tokenUserDomainValue";
        String crosscoreV2EndpointUrlValue = "http://example.com";
        String crosscoreV2TenantIdValue = "crosscoreV2TenantId";
        String crosscoreV2TokenIssuerValue = "crosscoreV2TokenIssuer";

        // TestDataStrategy
        String stubExperianEndpointValue = "http://localhostStub";
        String UatExperianEndpointValue = "http://localhostUat";
        String LiveExperianEndpointValue = "http://localhostLive";

        String stubTokenEndpointValue = "http://localhostStub";
        String UatTokenEndpointValue = "http://localhostUat";
        String LiveTokenEndpointValue = "http://localhostLive";

        // CRI Params
        when(mockParameterStoreService.getParameterValue(
                        ParameterPrefix.OVERRIDE,
                        FraudCheckConfigurationService.CONTRAINDICATION_MAPPINGS_PARAMETER_KEY))
                .thenReturn(contraindicationMappingsValue);
        when(mockParameterStoreService.getParameterValue(
                        ParameterPrefix.OVERRIDE,
                        FraudCheckConfigurationService.ZERO_SCORE_UCODES_PARAMETER_KEY))
                .thenReturn(zeroScoreUcodesValue);
        when(mockParameterStoreService.getParameterValue(
                        ParameterPrefix.OVERRIDE,
                        FraudCheckConfigurationService.NO_FILE_FOUND_THRESHOLD_PARAMETER_KEY))
                .thenReturn(String.valueOf(noFileFoundThresholdValue));

        String testStrategyRawEndpointValue =
                """
                {
                    "STUB": "http://localhostStub",
                    "UAT": "http://localhostUat",
                    "LIVE": "http://localhostLive"
                }
                """;
        String testStrategyTokenEndpointValue =
                """
                {
                    "STUB": "http://localhostStub",
                    "UAT": "http://localhostUat",
                    "LIVE": "http://localhostLive"
                }
                """;

        // Crosscore2
        Map<String, String> cc2TestParamMap =
                Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, String>(
                                CrosscoreV2Configuration.TOKEN_END_POINT_PARAMETER_KEY,
                                tokenEndpointValue),
                        new AbstractMap.SimpleEntry<String, String>(
                                CrosscoreV2Configuration.TOKEN_USER_DOMAIN_PARAMETER_KEY,
                                tokenUserDomainValue),
                        new AbstractMap.SimpleEntry<String, String>(
                                CrosscoreV2Configuration.TOKEN_USERNAME_PARAMETER_KEY,
                                tokenUserNameValue),
                        new AbstractMap.SimpleEntry<String, String>(
                                CrosscoreV2Configuration.TOKEN_PASSWORD_PARAMETER_KEY,
                                tokenPasswordValue),
                        new AbstractMap.SimpleEntry<String, String>(
                                CrosscoreV2Configuration.TOKEN_CLIENT_ID_PARAMETER_KEY,
                                tokenClientIdValue),
                        new AbstractMap.SimpleEntry<String, String>(
                                CrosscoreV2Configuration.TOKEN_CLIENT_SECRET_PARAMETER_KEY,
                                tokenClientSecretValue),
                        new AbstractMap.SimpleEntry<String, String>(
                                CrosscoreV2Configuration.TOKEN_ISSUER_PARAMETER_KEY,
                                crosscoreV2TokenIssuerValue),
                        new AbstractMap.SimpleEntry<String, String>(
                                CrosscoreV2Configuration.CC2_ENDPOINT_PARAMETER_KEY,
                                crosscoreV2EndpointUrlValue),
                        new AbstractMap.SimpleEntry<String, String>(
                                CrosscoreV2Configuration.CC2_TENANT_ID_PARAMETER_KEY,
                                crosscoreV2TenantIdValue),
                        new AbstractMap.SimpleEntry<String, String>(
                                CrosscoreV2Configuration.CC2_TEST_STRATEGY_ENDPOINT_PARAMETER_KEY,
                                testStrategyRawEndpointValue),
                        new AbstractMap.SimpleEntry<String, String>(
                                CrosscoreV2Configuration
                                        .TEST_STRATEGY_TOKEN_END_POINT_PARAMETER_KEY,
                                testStrategyTokenEndpointValue));

        when(mockParameterStoreService.getAllParametersFromPath(
                        ParameterPrefix.OVERRIDE, CrosscoreV2Configuration.CC2_PARAMETER_PATH))
                .thenReturn(cc2TestParamMap);
        // when(mockParameterStoreService.getAllParametersFromPath())

        when(mockParameterStoreService.getParameterValue(
                        ParameterPrefix.STACK,
                        CrosscoreV2Configuration.TOKEN_TABLE_NAME_PARAMETER_KEY))
                .thenReturn(experianTokenTableValue);

        // Creation
        FraudCheckConfigurationService fraudCheckConfigurationService =
                new FraudCheckConfigurationService(mockParameterStoreService, new ObjectMapper());

        assertNotNull(fraudCheckConfigurationService);

        // CRI Params
        assertEquals(
                contraindicationMappingsValue,
                fraudCheckConfigurationService.getContraindicationMappings());
        assertEquals(zeroScoreUcodesListValue, fraudCheckConfigurationService.getZeroScoreUcodes());
        assertEquals(
                noFileFoundThresholdValue,
                fraudCheckConfigurationService.getNoFileFoundThreshold(Strategy.NO_CHANGE));

        // CC2
        verify(mockParameterStoreService)
                .getAllParametersFromPath(
                        ParameterPrefix.OVERRIDE, CrosscoreV2Configuration.CC2_PARAMETER_PATH);
        assertEquals(
                tokenEndpointValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getTokenEndpoint());
        assertEquals(
                tokenUserDomainValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getUserDomain());
        assertEquals(
                tokenUserNameValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getUsername());
        assertEquals(
                tokenPasswordValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getPassword());
        assertEquals(
                tokenClientIdValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getClientId());

        assertEquals(
                tokenClientSecretValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getClientSecret());
        assertEquals(
                crosscoreV2TokenIssuerValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getTokenIssuer());
        // NOTE URI's
        assertEquals(
                crosscoreV2EndpointUrlValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getEndpointUri());
        assertEquals(
                crosscoreV2TenantIdValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getTenantId());

        verify(mockParameterStoreService)
                .getParameterValue(
                        ParameterPrefix.STACK,
                        CrosscoreV2Configuration.TOKEN_TABLE_NAME_PARAMETER_KEY);
        assertEquals(
                experianTokenTableValue,
                fraudCheckConfigurationService.getCrosscoreV2Configuration().getTokenTableName());
    }
}
