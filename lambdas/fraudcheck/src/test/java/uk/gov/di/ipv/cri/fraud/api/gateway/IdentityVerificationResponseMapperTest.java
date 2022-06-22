package uk.gov.di.ipv.cri.fraud.api.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.*;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityVerificationResponseMapperTest {
    private IdentityVerificationResponseMapper responseMapper;

    @BeforeEach
    void setup() {
        this.responseMapper = new IdentityVerificationResponseMapper();
    }

    @Test
    void mapIdentityVerificationResponseShouldMapInfoResponse() {
        IdentityVerificationResponse testIdentityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(ResponseType.INFO);

        DecisionElement decisionElement =
                testIdentityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0);
        Rule rule = new Rule();
        rule.setRuleName("");
        rule.setRuleId("rule01");
        rule.setRuleText("Test Rule");
        List<Rule> rules = List.of(rule);
        decisionElement.setRules(rules);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapIdentityVerificationResponse(
                        testIdentityVerificationResponse);

        assertNotNull(fraudCheckResult);
        assertTrue(fraudCheckResult.isExecutedSuccessfully());
        assertNull(fraudCheckResult.getErrorMessage());
        assertEquals(1, fraudCheckResult.getThirdPartyFraudCodes().length);
        assertEquals(rule.getRuleId(), fraudCheckResult.getThirdPartyFraudCodes()[0]);
    }

    @Test
    void mapIdentityVerificationInfoResponsesThatFailReturnFail() {
        IdentityVerificationResponse testIdentityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(ResponseType.INFO);

        testIdentityVerificationResponse.getResponseHeader().setTenantID(null);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapIdentityVerificationResponse(
                        testIdentityVerificationResponse);

        final String EXPECTED_ERROR =
                IdentityVerificationResponseMapper.IV_INFO_RESPONSE_VALIDATION_FAILED_MSG;

        assertNotNull(fraudCheckResult);
        assertFalse(fraudCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, fraudCheckResult.getErrorMessage());
    }

    @Test
    void mapIdentityVerificationResponseShouldMapWarnResponse() {
        String responseCode = "response-code";
        String responseMessage = "response-message";
        final ResponseType TYPE = ResponseType.WARN;

        IdentityVerificationResponse testIdentityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(TYPE);

        ResponseHeader responseHeader = testIdentityVerificationResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testIdentityVerificationResponse.setResponseHeader(responseHeader);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapIdentityVerificationResponse(
                        testIdentityVerificationResponse);

        assertEquals(responseHeader, testIdentityVerificationResponse.getResponseHeader());
        assertEquals(TYPE, testIdentityVerificationResponse.getResponseHeader().getResponseType());
        assertEquals(
                responseCode,
                testIdentityVerificationResponse.getResponseHeader().getResponseCode());
        assertEquals(
                responseMessage,
                testIdentityVerificationResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        responseCode,
                        responseMessage);

        assertNotNull(fraudCheckResult);
        assertFalse(fraudCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, fraudCheckResult.getErrorMessage());
        assertEquals(0, fraudCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapIdentityVerificationResponseShouldMapWarningResponse() {
        String responseCode = "response-code";
        String responseMessage = "response-message";
        final ResponseType TYPE = ResponseType.WARNING;

        IdentityVerificationResponse testIdentityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(TYPE);

        ResponseHeader responseHeader = testIdentityVerificationResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testIdentityVerificationResponse.setResponseHeader(responseHeader);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapIdentityVerificationResponse(
                        testIdentityVerificationResponse);

        assertEquals(responseHeader, testIdentityVerificationResponse.getResponseHeader());
        assertEquals(TYPE, testIdentityVerificationResponse.getResponseHeader().getResponseType());
        assertEquals(
                responseCode,
                testIdentityVerificationResponse.getResponseHeader().getResponseCode());
        assertEquals(
                responseMessage,
                testIdentityVerificationResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        responseCode,
                        responseMessage);

        assertNotNull(fraudCheckResult);
        assertFalse(fraudCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, fraudCheckResult.getErrorMessage());
        assertEquals(0, fraudCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapIdentityVerificationResponseShouldMapErrorResponse() {
        String responseCode = "response-code";
        String responseMessage = "response-message";
        final ResponseType TYPE = ResponseType.ERROR;

        IdentityVerificationResponse testIdentityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(TYPE);

        ResponseHeader responseHeader = testIdentityVerificationResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testIdentityVerificationResponse.setResponseHeader(responseHeader);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapIdentityVerificationResponse(
                        testIdentityVerificationResponse);

        assertEquals(responseHeader, testIdentityVerificationResponse.getResponseHeader());
        assertEquals(TYPE, testIdentityVerificationResponse.getResponseHeader().getResponseType());
        assertEquals(
                responseCode,
                testIdentityVerificationResponse.getResponseHeader().getResponseCode());
        assertEquals(
                responseMessage,
                testIdentityVerificationResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        responseCode,
                        responseMessage);

        assertNotNull(fraudCheckResult);
        assertFalse(fraudCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, fraudCheckResult.getErrorMessage());
        assertEquals(0, fraudCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapIdentityVerificationResponseFraudCheckErrorMessageCannotBeNullWithNullResponseCode() {
        String responseCode = null;
        String responseMessage = "response-message";
        final ResponseType TYPE = ResponseType.ERROR;

        IdentityVerificationResponse testIdentityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(TYPE);

        ResponseHeader responseHeader = testIdentityVerificationResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testIdentityVerificationResponse.setResponseHeader(responseHeader);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapIdentityVerificationResponse(
                        testIdentityVerificationResponse);

        assertEquals(responseHeader, testIdentityVerificationResponse.getResponseHeader());
        assertEquals(TYPE, testIdentityVerificationResponse.getResponseHeader().getResponseType());
        assertEquals(
                responseCode,
                testIdentityVerificationResponse.getResponseHeader().getResponseCode());
        assertEquals(
                responseMessage,
                testIdentityVerificationResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        IdentityVerificationResponseMapper
                                .IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK,
                        responseMessage);

        assertNotNull(fraudCheckResult);
        assertFalse(fraudCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, fraudCheckResult.getErrorMessage());
        assertEquals(0, fraudCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapIdentityVerificationResponseFraudCheckErrorMessageCannotBeEmptyWithEmptyResponseCode() {
        String responseCode = "";
        String responseMessage = "response-message";
        final ResponseType TYPE = ResponseType.ERROR;

        IdentityVerificationResponse testIdentityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(TYPE);

        ResponseHeader responseHeader = testIdentityVerificationResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testIdentityVerificationResponse.setResponseHeader(responseHeader);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapIdentityVerificationResponse(
                        testIdentityVerificationResponse);

        assertEquals(responseHeader, testIdentityVerificationResponse.getResponseHeader());
        assertEquals(TYPE, testIdentityVerificationResponse.getResponseHeader().getResponseType());
        assertEquals(
                responseCode,
                testIdentityVerificationResponse.getResponseHeader().getResponseCode());
        assertEquals(
                responseMessage,
                testIdentityVerificationResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        IdentityVerificationResponseMapper
                                .IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK,
                        responseMessage);

        assertNotNull(fraudCheckResult);
        assertFalse(fraudCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, fraudCheckResult.getErrorMessage());
        assertEquals(0, fraudCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void
            mapIdentityVerificationResponseFraudCheckErrorMessageCannotBeNullWithNullResponseMessage() {
        String responseCode = "response-code";
        String responseMessage = null;
        final ResponseType TYPE = ResponseType.ERROR;

        IdentityVerificationResponse testIdentityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(TYPE);

        ResponseHeader responseHeader = testIdentityVerificationResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testIdentityVerificationResponse.setResponseHeader(responseHeader);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapIdentityVerificationResponse(
                        testIdentityVerificationResponse);

        assertEquals(responseHeader, testIdentityVerificationResponse.getResponseHeader());
        assertEquals(TYPE, testIdentityVerificationResponse.getResponseHeader().getResponseType());
        assertEquals(
                responseCode,
                testIdentityVerificationResponse.getResponseHeader().getResponseCode());
        assertEquals(
                responseMessage,
                testIdentityVerificationResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        responseCode,
                        IdentityVerificationResponseMapper
                                .IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK);

        assertNotNull(fraudCheckResult);
        assertFalse(fraudCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, fraudCheckResult.getErrorMessage());
        assertEquals(0, fraudCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void
            mapIdentityVerificationResponseFraudCheckErrorMessageCannotBeEmptyWithEmptyResponseMessage() {
        String responseCode = "response-code";
        String responseMessage = "";
        final ResponseType TYPE = ResponseType.ERROR;

        IdentityVerificationResponse testIdentityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(TYPE);

        ResponseHeader responseHeader = testIdentityVerificationResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testIdentityVerificationResponse.setResponseHeader(responseHeader);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapIdentityVerificationResponse(
                        testIdentityVerificationResponse);

        assertEquals(responseHeader, testIdentityVerificationResponse.getResponseHeader());
        assertEquals(TYPE, testIdentityVerificationResponse.getResponseHeader().getResponseType());
        assertEquals(
                responseCode,
                testIdentityVerificationResponse.getResponseHeader().getResponseCode());
        assertEquals(
                responseMessage,
                testIdentityVerificationResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        responseCode,
                        IdentityVerificationResponseMapper
                                .IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK);

        assertNotNull(fraudCheckResult);
        assertFalse(fraudCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, fraudCheckResult.getErrorMessage());
        assertEquals(0, fraudCheckResult.getThirdPartyFraudCodes().length);
    }
}
