package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.ResponseType;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.WarningsErrors;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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

    private static IdentityVerificationResponse getResponseScenario(String scenario)
            throws Exception {

        IdentityVerificationResponse identityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(ResponseType.INFO);

        switch (scenario) {
            case "HappyPathInfo" -> {
                return identityVerificationResponse;
            }
            case "HappyPathError" -> {
                identityVerificationResponse =
                        TestDataCreator.createTestVerificationResponse(ResponseType.ERROR);

                return identityVerificationResponse;
            }
            case "Null Response" -> {
                return null;
            }
            case "Null ClientResponsePayload" -> identityVerificationResponse
                    .setClientResponsePayload(null);
            case "Null DecisionElements" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .setDecisionElements(null);
            case "DecisionElements Null Element" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .getDecisionElements()
                    .set(0, null);
            case "Null Response Header" -> identityVerificationResponse.setResponseHeader(null);
            case "Null Response Header ResponseType" -> identityVerificationResponse
                    .getResponseHeader()
                    .setResponseType(null);
            case "Null Response Header RequestType" -> identityVerificationResponse
                    .getResponseHeader()
                    .setRequestType(null);
            case "Null warningsErrors" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .getDecisionElements()
                    .get(0)
                    .setWarningsErrors(null);
            case "Empty warningsErrors" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .getDecisionElements()
                    .get(0)
                    .setWarningsErrors(new ArrayList<>());
            case "WarningsErrors null warning" -> {
                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .setWarningsErrors(new ArrayList<>());

                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .add(0, null);
            }
            case "Warning null responseType" -> {
                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .setWarningsErrors(new ArrayList<>());

                WarningsErrors warningsErrors = new WarningsErrors();
                warningsErrors.setResponseType(null);
                warningsErrors.setResponseCode("ResponseCode");
                warningsErrors.setResponseMessage("ResponseMessage");

                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .add(0, warningsErrors);
            }
            case "Warning null responseCode" -> {
                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .setWarningsErrors(new ArrayList<>());

                WarningsErrors warningsErrors = new WarningsErrors();
                warningsErrors.setResponseType("ResponseType");
                warningsErrors.setResponseCode(null);
                warningsErrors.setResponseMessage("ResponseMessage");

                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .add(0, warningsErrors);
            }
            case "Warning null responseMessage" -> {
                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .setWarningsErrors(new ArrayList<>());

                WarningsErrors warningsErrors = new WarningsErrors();
                warningsErrors.setResponseType("ResponseType");
                warningsErrors.setResponseCode("ResponseCode");
                warningsErrors.setResponseMessage(null);

                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .add(0, warningsErrors);
            }
            default -> throw new Exception("Invalid Test Scenario - " + scenario);
        }

        return identityVerificationResponse;
    }
}
