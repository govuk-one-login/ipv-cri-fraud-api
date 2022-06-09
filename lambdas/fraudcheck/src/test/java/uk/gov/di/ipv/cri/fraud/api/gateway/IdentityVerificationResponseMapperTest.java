package uk.gov.di.ipv.cri.fraud.api.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.DecisionElement;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.ResponseHeader;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.ResponseType;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.Rule;
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
    void mapIdentityVerificationResponseShouldMapWarnErrorResponse() {
        String responseCode = "response-code";
        String responseMessage = "response-message";
        IdentityVerificationResponse testIdentityVerificationResponse =
                new IdentityVerificationResponse();
        ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.setResponseType(ResponseType.ERROR);
        responseHeader.setResponseCode(responseCode);
        responseHeader.setResponseMessage(responseMessage);
        testIdentityVerificationResponse.setResponseHeader(responseHeader);

        FraudCheckResult fraudCheckResult =
                this.responseMapper.mapIdentityVerificationResponse(
                        testIdentityVerificationResponse);

        assertNotNull(fraudCheckResult);
        assertFalse(fraudCheckResult.isExecutedSuccessfully());
        assertEquals(
                "Error code: " + responseCode + ", error description: " + responseMessage,
                fraudCheckResult.getErrorMessage());
        assertEquals(0, fraudCheckResult.getThirdPartyFraudCodes().length);
    }

    @Test
    void mapIdentityVerificationResponseShouldMapInfoResponse() {
        IdentityVerificationResponse testIdentityVerificationResponse =
                TestDataCreator.createTestVerificationInfoResponse();

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
}
