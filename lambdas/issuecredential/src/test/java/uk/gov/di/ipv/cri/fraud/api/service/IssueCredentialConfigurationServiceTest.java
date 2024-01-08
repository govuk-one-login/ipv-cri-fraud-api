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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class IssueCredentialConfigurationServiceTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private final String AWS_STACK_NAME = "fraud-api-dev";
    private final String PARAMETER_PREFIX = AWS_STACK_NAME;

    private static final String KEY_FORMAT = "/%s/%s";
    @Mock private SecretsProvider mockSecretsProvider;
    @Mock private ParamProvider mockParamProvider;

    @Test
    void shouldInitialiseConfigFieldsWhenValidInputProvided() {

        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("PARAMETER_PREFIX", PARAMETER_PREFIX);
        environmentVariables.set("AWS_STACK_NAME", AWS_STACK_NAME);

        String env = "dev";

        String fraudResultTableName = "FraudTableName";
        String contraindicationMappings = "contraindicationMappings";
        String activityHistoryEnabled = "activityHistoryEnabled";

        String fraudResultTableNameValue = "fraudResultTableNameValue";
        String contraindicationMappingsValue = "contraindicationMappingsValue";
        boolean activityHistoryEnabledValue = true;

        when(mockParamProvider.get(
                        String.format(KEY_FORMAT, PARAMETER_PREFIX, contraindicationMappings)))
                .thenReturn(contraindicationMappingsValue);
        when(mockParamProvider.get(
                        String.format(KEY_FORMAT, PARAMETER_PREFIX, fraudResultTableName)))
                .thenReturn(fraudResultTableNameValue);
        when(mockParamProvider.get(
                        String.format(KEY_FORMAT, PARAMETER_PREFIX, activityHistoryEnabled)))
                .thenReturn(String.valueOf(activityHistoryEnabledValue));

        IssueCredentialConfigurationService issueCredentialConfigurationService =
                new IssueCredentialConfigurationService(
                        mockSecretsProvider, mockParamProvider, env);

        assertNotNull(issueCredentialConfigurationService);
        verify(mockParamProvider)
                .get(String.format(KEY_FORMAT, PARAMETER_PREFIX, contraindicationMappings));
        verify(mockParamProvider)
                .get(String.format(KEY_FORMAT, PARAMETER_PREFIX, fraudResultTableName));
        verify(mockParamProvider)
                .get(String.format(KEY_FORMAT, PARAMETER_PREFIX, activityHistoryEnabled));

        assertEquals(
                contraindicationMappingsValue,
                issueCredentialConfigurationService.getContraindicationMappings());
        assertEquals(
                fraudResultTableNameValue,
                issueCredentialConfigurationService.getFraudResultTableName());
        assertEquals(
                activityHistoryEnabledValue,
                issueCredentialConfigurationService.isActivityHistoryEnabled());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenSecretsProviderNull() {
        NullPointerException thrownException =
                assertThrows(
                        NullPointerException.class,
                        () -> new IssueCredentialConfigurationService(null, null, ""));
        assertEquals("secretsProvider must not be null", thrownException.getMessage());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenParamProviderNull() {
        NullPointerException thrownException =
                assertThrows(
                        NullPointerException.class,
                        () ->
                                new IssueCredentialConfigurationService(
                                        mockSecretsProvider, null, ""));
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
                                                    new IssueCredentialConfigurationService(
                                                            mockSecretsProvider,
                                                            mockParamProvider,
                                                            env));
                            assertEquals("env must be specified", thrownException.getMessage());
                        });
    }
}
