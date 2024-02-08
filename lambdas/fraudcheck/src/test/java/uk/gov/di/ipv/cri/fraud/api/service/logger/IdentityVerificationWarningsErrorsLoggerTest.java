package uk.gov.di.ipv.cri.fraud.api.service.logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator.getResponseScenario;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationWarningsErrorsLoggerTest {

    private IdentityVerificationWarningsErrorsLogger identityVerificationWarningsErrorsLogger;

    @BeforeEach
    void setUp() {
        identityVerificationWarningsErrorsLogger = new IdentityVerificationWarningsErrorsLogger();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "HappyPathInfo",
                "HappyPathError",
                "Null Response",
                "Null ClientResponsePayload",
                "Null DecisionElements",
                "DecisionElements Null Element",
                "Null Response Header",
                "Null Response Header ResponseType",
                "Null Response Header RequestType",
                "Null warningsErrors",
                "Empty warningsErrors",
                "WarningsErrors null warning",
                "Warning null responseType",
                "Warning null responseCode",
                "Warning null responseMessage"
            })
    void canHandleResponseScenarios(String scenario) throws Exception {

        IdentityVerificationResponse response = getResponseScenario(scenario);

        assertDoesNotThrow(
                () -> identityVerificationWarningsErrorsLogger.logAnyWarningsErrors(response));
    }
}
