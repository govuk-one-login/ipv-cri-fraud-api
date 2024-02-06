package uk.gov.di.ipv.cri.fraud.api.service.logger;

import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.ResponseType;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator.getResponseScenario;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationResponseLoggerTest {

    private IdentityVerificationResponseLogger identityVerificationResponseLogger;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // TODO
        identityVerificationResponseLogger = new IdentityVerificationResponseLogger(mapper);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "HappyPathInfo",
                "HappyPathError",
                "Null Response",
                "Null Response Header",
                "Null Response Header ResponseType",
                "Null Response Header RequestType",
                "Null ClientResponsePayload",
                "ClientResponsePayload null OrchestrationDecisions",
                "ClientResponsePayload null OrchestrationDecisions Element",
                "Null DecisionElements",
                "DecisionElements Null Element",
                "DecisionElements DecisionElement null decision",
                "DecisionElements DecisionElement null score",
                "DecisionElements DecisionElement null decisionReason"
            })
    void canHandleResponseScenarios(String scenario) throws Exception {

        IdentityVerificationResponse response = getResponseScenario(scenario);

        assertDoesNotThrow(() -> identityVerificationResponseLogger.logResponseFields(response));
    }

    @Test
    void shouldLogErrorForErrorsExtractingFieldsFromResponse() throws Exception {

        // Set up a mockMapper for this test and IdentityVerificationResponseLogger
        ObjectMapper mockMapper = Mockito.mock(ObjectMapper.class);
        IdentityVerificationResponseLogger thisTestOnlyidentityVerificationResponseLogger =
                new IdentityVerificationResponseLogger(mockMapper);

        // Trigger an exception when we go to create the final json string
        Exception exceptionCaught =
                new InputCoercionException(null, "Problem during json mapping", null, null);
        doThrow(exceptionCaught).when(mockMapper).writeValueAsString(any(Map.class));

        IdentityVerificationResponse response =
                TestDataCreator.createTestVerificationResponse(ResponseType.INFO);

        // Confirm the mapping exception is handled
        assertDoesNotThrow(
                () -> thisTestOnlyidentityVerificationResponseLogger.logResponseFields(response));
    }
}
