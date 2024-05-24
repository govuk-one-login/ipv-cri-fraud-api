package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.check.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.check.PepCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.*;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_FAIL;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_PASS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_TYPE_INFO;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_FAIL;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_PASS;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationResponseMapperTest {

    @Mock private EventProbe mockEventProbe;
    private IdentityVerificationResponseMapper responseMapper;

    @BeforeEach
    void setup() {
        this.responseMapper =
                new IdentityVerificationResponseMapper(mockEventProbe, new ObjectMapper());
    }

    @Test
    void mapFraudResponseShouldMapInfoResponse() {
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
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO);
        inOrder.verify(mockEventProbe)
                .counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_PASS);

        assertNotNull(fraudCheckResult);
        assertTrue(fraudCheckResult.isExecutedSuccessfully());
        assertNull(fraudCheckResult.getErrorMessage());
        assertEquals(1, fraudCheckResult.getThirdPartyFraudCodes().length);
        assertEquals(90, fraudCheckResult.getDecisionScore());
        assertEquals(rule.getRuleId(), fraudCheckResult.getThirdPartyFraudCodes()[0]);
    }

    @Test
    void mapIdentityVerificationInfoResponsesThatFailReturnFail() {
        IdentityVerificationResponse testIdentityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(ResponseType.INFO);

        testIdentityVerificationResponse.getResponseHeader().setTenantID(null);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO);
        inOrder.verify(mockEventProbe)
                .counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_FAIL);

        final String EXPECTED_ERROR =
                IdentityVerificationResponseMapper.IV_INFO_RESPONSE_VALIDATION_FAILED_MSG;

        assertNotNull(fraudCheckResult);
        assertFalse(fraudCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, fraudCheckResult.getErrorMessage());
    }

    @Test
    void mapFraudResponseShouldMapWarnResponse() {
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
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR);

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
    void mapFraudResponseShouldMapWarningResponse() {
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
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR);

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
    void mapFraudResponseShouldMapErrorResponse() {
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
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR);

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
    void mapFraudResponseFraudCheckErrorMessageCannotBeNullWithNullResponseCode() {
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
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR);

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
    void mapFraudResponseFraudCheckErrorMessageCannotBeEmptyWithEmptyResponseCode() {
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
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR);

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
    void mapFraudResponseFraudCheckErrorMessageCannotBeNullWithNullResponseMessage() {
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
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR);

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
    void mapFraudResponseFraudCheckErrorMessageCannotBeEmptyWithEmptyResponseMessage() {
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
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR);

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
    void mapPEPResponseShouldMapInfoResponse() {
        PEPResponse testPEPResponse = TestDataCreator.createTestPEPResponse(ResponseType.INFO);

        DecisionElement decisionElement =
                testPEPResponse.getClientResponsePayload().getDecisionElements().get(0);
        Rule rule = new Rule();
        rule.setRuleName("");
        rule.setRuleId("rule01");
        rule.setRuleText("Test Rule");
        List<Rule> rules = List.of(rule);
        decisionElement.setRules(rules);

        PepCheckResult pepCheckResult = this.responseMapper.mapPEPResponse(testPEPResponse);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_INFO);
        inOrder.verify(mockEventProbe)
                .counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_PASS);

        assertNotNull(pepCheckResult);
        assertTrue(pepCheckResult.isExecutedSuccessfully());
        assertNull(pepCheckResult.getErrorMessage());
        assertEquals(1, pepCheckResult.getThirdPartyFraudCodes().length);
        assertEquals(rule.getRuleId(), pepCheckResult.getThirdPartyFraudCodes()[0]);
    }

    @Test
    void mapPEPResponsesThatFailReturnFail() {
        PEPResponse testPEPResponse = TestDataCreator.createTestPEPResponse(ResponseType.INFO);

        testPEPResponse.getResponseHeader().setTenantID(null);

        PepCheckResult pepCheckResult = this.responseMapper.mapPEPResponse(testPEPResponse);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_INFO);
        inOrder.verify(mockEventProbe)
                .counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_FAIL);

        final String EXPECTED_ERROR =
                IdentityVerificationResponseMapper.IV_INFO_RESPONSE_VALIDATION_FAILED_MSG;

        assertNotNull(pepCheckResult);
        assertFalse(pepCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, pepCheckResult.getErrorMessage());
    }

    @Test
    void mapPEPResponseShouldMapWarnResponse() {
        String responseCode = "response-code";
        String responseMessage = "response-message";
        final ResponseType TYPE = ResponseType.WARN;

        PEPResponse testPEPResponse = TestDataCreator.createTestPEPResponse(TYPE);

        ResponseHeader responseHeader = testPEPResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testPEPResponse.setResponseHeader(responseHeader);

        PepCheckResult pepCheckResult = this.responseMapper.mapPEPResponse(testPEPResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR);

        assertEquals(responseHeader, testPEPResponse.getResponseHeader());
        assertEquals(TYPE, testPEPResponse.getResponseHeader().getResponseType());
        assertEquals(responseCode, testPEPResponse.getResponseHeader().getResponseCode());
        assertEquals(responseMessage, testPEPResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        responseCode,
                        responseMessage);

        assertNotNull(pepCheckResult);
        assertFalse(pepCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, pepCheckResult.getErrorMessage());
        assertEquals(0, pepCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapPEPResponseShouldMapWarningResponse() {
        String responseCode = "response-code";
        String responseMessage = "response-message";
        final ResponseType TYPE = ResponseType.WARNING;

        PEPResponse testPEPResponse = TestDataCreator.createTestPEPResponse(TYPE);

        ResponseHeader responseHeader = testPEPResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testPEPResponse.setResponseHeader(responseHeader);

        PepCheckResult pepCheckResult = this.responseMapper.mapPEPResponse(testPEPResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR);

        assertEquals(responseHeader, testPEPResponse.getResponseHeader());
        assertEquals(TYPE, testPEPResponse.getResponseHeader().getResponseType());
        assertEquals(responseCode, testPEPResponse.getResponseHeader().getResponseCode());
        assertEquals(responseMessage, testPEPResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        responseCode,
                        responseMessage);

        assertNotNull(pepCheckResult);
        assertFalse(pepCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, pepCheckResult.getErrorMessage());
        assertEquals(0, pepCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapPEPResponseShouldMapErrorResponse() {
        String responseCode = "response-code";
        String responseMessage = "response-message";
        final ResponseType TYPE = ResponseType.ERROR;

        PEPResponse testPEPResponse = TestDataCreator.createTestPEPResponse(TYPE);

        ResponseHeader responseHeader = testPEPResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testPEPResponse.setResponseHeader(responseHeader);

        PepCheckResult pepCheckResult = this.responseMapper.mapPEPResponse(testPEPResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR);

        assertEquals(responseHeader, testPEPResponse.getResponseHeader());
        assertEquals(TYPE, testPEPResponse.getResponseHeader().getResponseType());
        assertEquals(responseCode, testPEPResponse.getResponseHeader().getResponseCode());
        assertEquals(responseMessage, testPEPResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        responseCode,
                        responseMessage);

        assertNotNull(pepCheckResult);
        assertFalse(pepCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, pepCheckResult.getErrorMessage());
        assertEquals(0, pepCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapPEPResponseFraudCheckErrorMessageCannotBeNullWithNullResponseCode() {
        String responseCode = null;
        String responseMessage = "response-message";
        final ResponseType TYPE = ResponseType.ERROR;

        PEPResponse testPEPResponse = TestDataCreator.createTestPEPResponse(TYPE);

        ResponseHeader responseHeader = testPEPResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testPEPResponse.setResponseHeader(responseHeader);

        PepCheckResult pepCheckResult = this.responseMapper.mapPEPResponse(testPEPResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR);

        assertEquals(responseHeader, testPEPResponse.getResponseHeader());
        assertEquals(TYPE, testPEPResponse.getResponseHeader().getResponseType());
        assertEquals(responseCode, testPEPResponse.getResponseHeader().getResponseCode());
        assertEquals(responseMessage, testPEPResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        IdentityVerificationResponseMapper
                                .IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK,
                        responseMessage);

        assertNotNull(pepCheckResult);
        assertFalse(pepCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, pepCheckResult.getErrorMessage());
        assertEquals(0, pepCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapPEPResponseFraudCheckErrorMessageCannotBeEmptyWithEmptyResponseCode() {
        String responseCode = "";
        String responseMessage = "response-message";
        final ResponseType TYPE = ResponseType.ERROR;

        PEPResponse testPEPResponse = TestDataCreator.createTestPEPResponse(TYPE);

        ResponseHeader responseHeader = testPEPResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testPEPResponse.setResponseHeader(responseHeader);

        PepCheckResult pepCheckResult = this.responseMapper.mapPEPResponse(testPEPResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR);

        assertEquals(responseHeader, testPEPResponse.getResponseHeader());
        assertEquals(TYPE, testPEPResponse.getResponseHeader().getResponseType());
        assertEquals(responseCode, testPEPResponse.getResponseHeader().getResponseCode());
        assertEquals(responseMessage, testPEPResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        IdentityVerificationResponseMapper
                                .IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK,
                        responseMessage);

        assertNotNull(pepCheckResult);
        assertFalse(pepCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, pepCheckResult.getErrorMessage());
        assertEquals(0, pepCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapPEPResponseFraudCheckErrorMessageCannotBeNullWithNullResponseMessage() {
        String responseCode = "response-code";
        String responseMessage = null;
        final ResponseType TYPE = ResponseType.ERROR;

        PEPResponse testPEPResponse = TestDataCreator.createTestPEPResponse(TYPE);

        ResponseHeader responseHeader = testPEPResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testPEPResponse.setResponseHeader(responseHeader);

        PepCheckResult pepCheckResult = this.responseMapper.mapPEPResponse(testPEPResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR);

        assertEquals(responseHeader, testPEPResponse.getResponseHeader());
        assertEquals(TYPE, testPEPResponse.getResponseHeader().getResponseType());
        assertEquals(responseCode, testPEPResponse.getResponseHeader().getResponseCode());
        assertEquals(responseMessage, testPEPResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        responseCode,
                        IdentityVerificationResponseMapper
                                .IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK);

        assertNotNull(pepCheckResult);
        assertFalse(pepCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, pepCheckResult.getErrorMessage());
        assertEquals(0, pepCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapPEPResponseFraudCheckErrorMessageCannotBeEmptyWithEmptyResponseMessage() {
        String responseCode = "response-code";
        String responseMessage = "";
        final ResponseType TYPE = ResponseType.ERROR;

        PEPResponse testPEPResponse = TestDataCreator.createTestPEPResponse(TYPE);

        ResponseHeader responseHeader = testPEPResponse.getResponseHeader();
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);

        testPEPResponse.setResponseHeader(responseHeader);

        PepCheckResult pepCheckResult = this.responseMapper.mapPEPResponse(testPEPResponse);

        verify(mockEventProbe).counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR);

        assertEquals(responseHeader, testPEPResponse.getResponseHeader());
        assertEquals(TYPE, testPEPResponse.getResponseHeader().getResponseType());
        assertEquals(responseCode, testPEPResponse.getResponseHeader().getResponseCode());
        assertEquals(responseMessage, testPEPResponse.getResponseHeader().getResponseMessage());

        final String EXPECTED_ERROR =
                String.format(
                        IdentityVerificationResponseMapper.IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        responseCode,
                        IdentityVerificationResponseMapper
                                .IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK);

        assertNotNull(pepCheckResult);
        assertFalse(pepCheckResult.isExecutedSuccessfully());
        assertEquals(EXPECTED_ERROR, pepCheckResult.getErrorMessage());
        assertEquals(0, pepCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapFraudResponseWithEmptyDataCountsShouldReturnInfoResponse() {
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

        List<DataCount> dataCounts = new ArrayList<>();
        decisionElement.setDataCounts(dataCounts);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO);
        inOrder.verify(mockEventProbe)
                .counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_PASS);

        assertNotNull(fraudCheckResult);
        assertTrue(fraudCheckResult.isExecutedSuccessfully());
        assertNull(fraudCheckResult.getErrorMessage());
        assertEquals(1, fraudCheckResult.getThirdPartyFraudCodes().length);
        assertEquals(90, fraudCheckResult.getDecisionScore());
        assertEquals(rule.getRuleId(), fraudCheckResult.getThirdPartyFraudCodes()[0]);

        assertEquals(
                dataCounts,
                testIdentityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDataCounts());
    }

    @Test
    void mapFraudResponseWithNullDataCountsShouldReturnInfoResponse() {
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

        List<DataCount> dataCounts = null;
        decisionElement.setDataCounts(dataCounts);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO);
        inOrder.verify(mockEventProbe)
                .counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_PASS);

        assertNotNull(fraudCheckResult);
        assertTrue(fraudCheckResult.isExecutedSuccessfully());
        assertNull(fraudCheckResult.getErrorMessage());
        assertEquals(1, fraudCheckResult.getThirdPartyFraudCodes().length);
        assertEquals(90, fraudCheckResult.getDecisionScore());
        assertEquals(rule.getRuleId(), fraudCheckResult.getThirdPartyFraudCodes()[0]);

        assertEquals(
                dataCounts,
                testIdentityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDataCounts());
    }

    @Test
    void mapFraudResponseWithInvalidDataCountsShouldReturnInfoResponse() {
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

        List<DataCount> dataCounts = new ArrayList<>();
        DataCount IDandLocDataAtCL_StartDateOldestPrim = new DataCount();
        DataCount IDandLocDataAtCL_StartDateOldestSec = new DataCount();
        DataCount LocDataOnlyAtCLoc_StartDateOldestPrim = new DataCount();

        IDandLocDataAtCL_StartDateOldestPrim.setName("IDandLocDataAtCL_StartDateOldestPrim");
        IDandLocDataAtCL_StartDateOldestPrim.setValue(203212);
        IDandLocDataAtCL_StartDateOldestSec.setName("IDandLocDataAtCL_StartDateOldestSec");
        IDandLocDataAtCL_StartDateOldestSec.setValue(301212);
        LocDataOnlyAtCLoc_StartDateOldestPrim.setName("LocDataOnlyAtCLoc_StartDateOldestPrim");
        LocDataOnlyAtCLoc_StartDateOldestPrim.setValue(201512);

        dataCounts.add(IDandLocDataAtCL_StartDateOldestPrim);
        dataCounts.add(IDandLocDataAtCL_StartDateOldestSec);
        dataCounts.add(LocDataOnlyAtCLoc_StartDateOldestPrim);
        decisionElement.setDataCounts(dataCounts);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO);
        inOrder.verify(mockEventProbe)
                .counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_PASS);

        assertNotNull(fraudCheckResult);
        assertTrue(fraudCheckResult.isExecutedSuccessfully());
        assertNull(fraudCheckResult.getErrorMessage());
        assertEquals(1, fraudCheckResult.getThirdPartyFraudCodes().length);
        assertEquals(90, fraudCheckResult.getDecisionScore());
        assertEquals(rule.getRuleId(), fraudCheckResult.getThirdPartyFraudCodes()[0]);

        assertEquals(
                dataCounts,
                testIdentityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDataCounts());
    }

    @Test
    void mapFraudResponseWithValidDataCountValuesShouldReturnInfoResponse() {
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

        List<DataCount> dataCounts = new ArrayList<>();
        DataCount IDandLocDataAtCL_StartDateOldestPrim = new DataCount();
        DataCount IDandLocDataAtCL_StartDateOldestSec = new DataCount();
        DataCount LocDataOnlyAtCLoc_StartDateOldestPrim = new DataCount();

        IDandLocDataAtCL_StartDateOldestPrim.setName("IDandLocDataAtCL_StartDateOldestPrim");
        IDandLocDataAtCL_StartDateOldestPrim.setValue(201212);
        IDandLocDataAtCL_StartDateOldestSec.setName("IDandLocDataAtCL_StartDateOldestSec");
        IDandLocDataAtCL_StartDateOldestSec.setValue(201512);
        LocDataOnlyAtCLoc_StartDateOldestPrim.setName("LocDataOnlyAtCLoc_StartDateOldestPrim");
        LocDataOnlyAtCLoc_StartDateOldestPrim.setValue(201510);

        dataCounts.add(IDandLocDataAtCL_StartDateOldestPrim);
        dataCounts.add(IDandLocDataAtCL_StartDateOldestSec);
        dataCounts.add(LocDataOnlyAtCLoc_StartDateOldestPrim);
        decisionElement.setDataCounts(dataCounts);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapFraudResponse(testIdentityVerificationResponse);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO);
        inOrder.verify(mockEventProbe)
                .counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_PASS);

        assertNotNull(fraudCheckResult);
        assertTrue(fraudCheckResult.isExecutedSuccessfully());
        assertNull(fraudCheckResult.getErrorMessage());
        assertEquals(1, fraudCheckResult.getThirdPartyFraudCodes().length);
        assertEquals(90, fraudCheckResult.getDecisionScore());
        assertEquals(rule.getRuleId(), fraudCheckResult.getThirdPartyFraudCodes()[0]);

        assertEquals(
                dataCounts,
                testIdentityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDataCounts());
    }
}
