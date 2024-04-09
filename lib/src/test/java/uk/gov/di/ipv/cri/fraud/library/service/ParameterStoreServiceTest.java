package uk.gov.di.ipv.cri.fraud.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.lambda.powertools.parameters.SSMProvider;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class ParameterStoreServiceTest {

    private static final String TEST_PARAM_NAME = "TEST-Parameter";
    private static final String TEST_PARAM_VALUE = "TEST-Parameter-Value";

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock private ClientFactoryService mockClientFactoryService;
    @Mock SSMProvider mockSSMProvider;

    private final String AWS_STACK_NAME = "cri-api-dev";
    private final String PARAMETER_PREFIX = "cri-api-pipeline";
    private final String COMMON_PARAMETER_NAME_PREFIX = "commmon-lambdas";

    private ParameterStoreService parameterStoreService;

    @BeforeEach
    void setUp() {
        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("ENVIRONMENT", "TEST");

        environmentVariables.set("PARAMETER_PREFIX", PARAMETER_PREFIX);
        environmentVariables.set("AWS_STACK_NAME", AWS_STACK_NAME);
        environmentVariables.set("COMMON_PARAMETER_NAME_PREFIX", COMMON_PARAMETER_NAME_PREFIX);

        when(mockClientFactoryService.getSSMProvider()).thenReturn(mockSSMProvider);

        parameterStoreService = new ParameterStoreService(mockClientFactoryService);
    }

    @ParameterizedTest
    @CsvSource({"OVERRIDE", "STACK", "COMMON_API", "ENV"})
    void shouldGetParamValueByParameterName(ParameterPrefix prefixToTest) {
        String fullParamName =
                String.format("/%s/%s", prefixToTest.getPrefixValue(), TEST_PARAM_NAME);

        when(mockSSMProvider.get(fullParamName)).thenReturn(TEST_PARAM_VALUE);

        assertEquals(
                TEST_PARAM_VALUE,
                parameterStoreService.getParameterValue(prefixToTest, TEST_PARAM_NAME));

        verify(mockSSMProvider).get(fullParamName);
    }

    @ParameterizedTest
    @CsvSource({"OVERRIDE", "STACK", "COMMON_API", "ENV"})
    void shouldGetEncryptedParameterValueByParameterName(ParameterPrefix prefixToTest) {
        String fullParamName =
                String.format("/%s/%s", prefixToTest.getPrefixValue(), TEST_PARAM_NAME);

        when(mockSSMProvider.withDecryption()).thenReturn(mockSSMProvider);
        when(mockSSMProvider.get(fullParamName)).thenReturn(TEST_PARAM_VALUE);

        assertEquals(
                TEST_PARAM_VALUE,
                parameterStoreService.getEncryptedParameterValue(prefixToTest, TEST_PARAM_NAME));

        verify(mockSSMProvider).withDecryption();
        verify(mockSSMProvider).get(fullParamName);
    }

    @ParameterizedTest
    @CsvSource({"OVERRIDE", "STACK", "COMMON_API", "ENV"})
    void shouldGetAllParametersFromPath(ParameterPrefix prefixToTest) {
        Map<String, String> testParameterMap = Map.of("KEY1", "TEST_VALUE1", "KEY2", "TEST_VALUE2");

        String testPath = "TESTPATH/SUBPATH";
        String fullPath = String.format("/%s/%s", prefixToTest.getPrefixValue(), testPath);

        when(mockSSMProvider.recursive()).thenReturn(mockSSMProvider);
        when(mockSSMProvider.getMultiple(fullPath)).thenReturn(testParameterMap);

        assertEquals(
                testParameterMap,
                parameterStoreService.getAllParametersFromPath(prefixToTest, testPath));

        verify(mockSSMProvider).recursive();
        verify(mockSSMProvider).getMultiple(fullPath);
    }

    @ParameterizedTest
    @CsvSource({"OVERRIDE", "STACK", "COMMON_API", "ENV"})
    void shouldGetAllParametersFromPathWithDecryption(ParameterPrefix prefixToTest) {
        Map<String, String> testParameterMap = Map.of("KEY1", "TEST_VALUE1", "KEY2", "TEST_VALUE2");

        String testPath = "TESTPATH/SUBPATH";
        String fullPath = String.format("/%s/%s", prefixToTest.getPrefixValue(), testPath);

        when(mockSSMProvider.withDecryption()).thenReturn(mockSSMProvider);
        when(mockSSMProvider.getMultiple(fullPath)).thenReturn(testParameterMap);

        assertEquals(
                testParameterMap,
                parameterStoreService.getAllParametersFromPathWithDecryption(
                        prefixToTest, testPath));

        verify(mockSSMProvider).getMultiple(fullPath);

        verify(mockSSMProvider).withDecryption();
        verify(mockSSMProvider).getMultiple(fullPath);
    }
}
