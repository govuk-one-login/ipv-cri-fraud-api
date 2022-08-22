package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.ResponseType;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.Rule;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.WarningsErrors;
import uk.gov.di.ipv.cri.fraud.api.util.JsonValidationUtility;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IdentityVerificationInfoResponseValidatorTest {
    private static IdentityVerificationInfoResponseValidator infoResponseValidator;
    private static IdentityVerificationResponse testIVResponse;
    private static final String len10String = "0123456789";

    @BeforeAll
    public static void GlobalSetup() {
        infoResponseValidator = new IdentityVerificationInfoResponseValidator();
    }

    @BeforeEach
    public void PreEachTestSetup() {
        testIVResponse = TestDataCreator.createTestVerificationResponse(ResponseType.INFO);
    }

    @Test
    void nullResponseIsAnError() {
        ValidationResult<List<String>> validationResult = infoResponseValidator.validate(null);

        assertEquals(1, validationResult.getError().size());
        assertEquals(
                IdentityVerificationInfoResponseValidator.NULL_RESPONSE_ERROR_MESSAGE,
                validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testResponseCanBeValidated() {
        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerCannotBeNULL() {
        testIVResponse.setResponseHeader(null);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(1, validationResult.getError().size());
        assertEquals(
                IdentityVerificationInfoResponseValidator.NULL_HEADER_ERROR_MESSAGE,
                validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerTenantIDCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(3) + "1";
        testIVResponse.getResponseHeader().setTenantID(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "TenantID" + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getTenantID());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerTenantIDMaxLenOK() {
        final String TEST_STRING = len10String.repeat(3);
        testIVResponse.getResponseHeader().setTenantID(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getTenantID());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerTenantIDCannotBeBlank() {
        final String TEST_STRING = "";
        testIVResponse.getResponseHeader().setTenantID(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "TenantID" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(testIVResponse.getResponseHeader().getTenantID(), TEST_STRING);
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerRequestTypeCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(4) + "1";
        testIVResponse.getResponseHeader().setRequestType(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "RequestType" + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getRequestType());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerRequestTypeMaxLenOK() {
        final String TEST_STRING = len10String.repeat(4);
        testIVResponse.getResponseHeader().setRequestType(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getRequestType());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerRequestTypeCannotBeBlank() {
        final String TEST_STRING = "";
        testIVResponse.getResponseHeader().setRequestType(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "RequestType" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(testIVResponse.getResponseHeader().getRequestType(), TEST_STRING);
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerClientReferenceIdCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(9) + "1";
        testIVResponse.getResponseHeader().setClientReferenceId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "ClientReferenceId" + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getClientReferenceId());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerClientReferenceIdMaxLenOK() {
        final String TEST_STRING = len10String.repeat(4);
        testIVResponse.getResponseHeader().setClientReferenceId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getClientReferenceId());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerClientReferenceIdCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse.getResponseHeader().setClientReferenceId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "ClientReferenceId" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getClientReferenceId());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerExpRequestIdCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(9) + "1";
        testIVResponse.getResponseHeader().setExpRequestId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "ExpRequestId" + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getExpRequestId());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerExpRequestIdMaxLenOK() {
        final String TEST_STRING = len10String.repeat(9);
        testIVResponse.getResponseHeader().setExpRequestId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getExpRequestId());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerExpRequestIdCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse.getResponseHeader().setExpRequestId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "ExpRequestId" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getExpRequestId());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerMessageTimeFailsIfNull() {
        final String TEST_STRING = null;
        testIVResponse.getResponseHeader().setMessageTime(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "MessageTime" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        assertNull(testIVResponse.getResponseHeader().getMessageTime());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerMessageTimeFailsIfEmpty() {
        final String TEST_STRING = "";
        testIVResponse.getResponseHeader().setMessageTime(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "MessageTime" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getMessageTime());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerMessageTimeFailsIfInvalid() {
        final String TEST_STRING = "123";
        testIVResponse.getResponseHeader().setMessageTime(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "MessageTime" + JsonValidationUtility.FAIL_PARSING_TIMESTAMP_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getMessageTime());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerMessageValidTimeCanBeParsed() {
        final String TEST_STRING = "2022-01-01T00:00:01Z";
        testIVResponse.getResponseHeader().setMessageTime(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getMessageTime());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerOverallResponseCannotBeNull() {
        testIVResponse.getResponseHeader().setOverallResponse(null);
        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertNull(testIVResponse.getResponseHeader().getOverallResponse());
        assertEquals(1, validationResult.getError().size());
        assertEquals(
                IdentityVerificationInfoResponseValidator
                        .NULL_HEADER_OVERALL_RESPONSE_ERROR_MESSAGE,
                validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testHeaderOverallResponseDecisionCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(1) + "1";

        testIVResponse.getResponseHeader().getOverallResponse().setDecision(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OverallResponse:Decision" + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING, testIVResponse.getResponseHeader().getOverallResponse().getDecision());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testHeaderOverallResponseDecisionMaxLenOK() {
        final String TEST_STRING = len10String.repeat(1);
        testIVResponse.getResponseHeader().getOverallResponse().setDecision(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING, testIVResponse.getResponseHeader().getOverallResponse().getDecision());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionCannotEmpty() {
        final String TEST_STRING = "";
        testIVResponse.getResponseHeader().getOverallResponse().setDecision(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OverallResponse:Decision" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING, testIVResponse.getResponseHeader().getOverallResponse().getDecision());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionScoreUnderMinFails() {
        final int TEST_VALUE = -1;
        testIVResponse.getResponseHeader().getOverallResponse().setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String TEST_INTEGER_NAME = "OverallResponse:Score";

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_VALUE,
                        IdentityVerificationInfoResponseValidator
                                .OVERALL_RESPONSE_DECISION_SCORE_MIN_VALUE,
                        IdentityVerificationInfoResponseValidator
                                .OVERALL_RESPONSE_DECISION_SCORE_MAX_VALUE,
                        TEST_INTEGER_NAME);

        assertEquals(
                TEST_VALUE, testIVResponse.getResponseHeader().getOverallResponse().getScore());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionScoreMinOK() {
        final int TEST_VALUE = 0;
        testIVResponse.getResponseHeader().getOverallResponse().setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_VALUE, testIVResponse.getResponseHeader().getOverallResponse().getScore());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionScoreMaxOK() {
        final int TEST_VALUE = 99999;
        testIVResponse.getResponseHeader().getOverallResponse().setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_VALUE, testIVResponse.getResponseHeader().getOverallResponse().getScore());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionScoreOverMaxFails() {
        final int TEST_VALUE = 100000;
        testIVResponse.getResponseHeader().getOverallResponse().setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String TEST_INTEGER_NAME = "OverallResponse:Score";

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_VALUE,
                        IdentityVerificationInfoResponseValidator
                                .OVERALL_RESPONSE_DECISION_SCORE_MIN_VALUE,
                        IdentityVerificationInfoResponseValidator
                                .OVERALL_RESPONSE_DECISION_SCORE_MAX_VALUE,
                        TEST_INTEGER_NAME);

        assertEquals(
                TEST_VALUE, testIVResponse.getResponseHeader().getOverallResponse().getScore());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionTextCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(3) + "1";

        testIVResponse.getResponseHeader().getOverallResponse().setDecisionText(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OverallResponse:DecisionText"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse.getResponseHeader().getOverallResponse().getDecisionText());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionTextMaxLenOK() {
        final String TEST_STRING = len10String.repeat(3);

        testIVResponse.getResponseHeader().getOverallResponse().setDecisionText(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse.getResponseHeader().getOverallResponse().getDecisionText());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionTextCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse.getResponseHeader().getOverallResponse().setDecisionText(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OverallResponse:DecisionText"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse.getResponseHeader().getOverallResponse().getDecisionText());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionReasonsCannotBeEmpty() {
        testIVResponse
                .getResponseHeader()
                .getOverallResponse()
                .setDecisionReasons(new ArrayList<>());

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OverallResponse:DecisionReasons"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                0,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getDecisionReasons()
                        .size());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionReasonsReasonCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(9) + "1";
        testIVResponse
                .getResponseHeader()
                .getOverallResponse()
                .setDecisionReasons(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OverallResponse:DecisionReasons:Reason"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                1,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getDecisionReasons()
                        .size());
        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getDecisionReasons()
                        .get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionReasonsReasonMaxLenOK() {
        final String TEST_STRING = len10String.repeat(9);
        testIVResponse
                .getResponseHeader()
                .getOverallResponse()
                .setDecisionReasons(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getDecisionReasons()
                        .size());
        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getDecisionReasons()
                        .get(0));
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerOverallResponseDecisionReasonsReasonCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse
                .getResponseHeader()
                .getOverallResponse()
                .setDecisionReasons(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OverallResponse:DecisionReasons:Reason"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                1,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getDecisionReasons()
                        .size());
        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getDecisionReasons()
                        .get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerOverallResponseRecommendedNextActionsEmptyIsOK() {
        testIVResponse
                .getResponseHeader()
                .getOverallResponse()
                .setRecommendedNextActions(new ArrayList<>());

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                0,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getRecommendedNextActions()
                        .size());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerOverallResponseRecommendedNextActionsActionCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(20) + "1";
        testIVResponse
                .getResponseHeader()
                .getOverallResponse()
                .setRecommendedNextActions(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OverallResponse:RecommendedNextActions:Action"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                1,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getRecommendedNextActions()
                        .size());
        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getRecommendedNextActions()
                        .get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerOverallResponseRecommendedNextActionsActionMaxLenOK() {
        final String TEST_STRING = len10String.repeat(20);
        testIVResponse
                .getResponseHeader()
                .getOverallResponse()
                .setRecommendedNextActions(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getRecommendedNextActions()
                        .size());
        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getRecommendedNextActions()
                        .get(0));
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerOverallResponseRecommendedNextActionsEmptyActionIsOK() {
        final String TEST_STRING = "";
        testIVResponse
                .getResponseHeader()
                .getOverallResponse()
                .setRecommendedNextActions(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getRecommendedNextActions()
                        .size());
        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getResponseHeader()
                        .getOverallResponse()
                        .getRecommendedNextActions()
                        .get(0));
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerOverallResponseSpareObjectsEmptyIsOK() {
        testIVResponse.getResponseHeader().getOverallResponse().setSpareObjects(new ArrayList<>());

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                0,
                testIVResponse.getResponseHeader().getOverallResponse().getSpareObjects().size());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerOverallResponseSpareObjectsObjectCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(20) + "1";
        testIVResponse
                .getResponseHeader()
                .getOverallResponse()
                .setSpareObjects(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OverallResponse:SpareObjects:Object"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                1,
                testIVResponse.getResponseHeader().getOverallResponse().getSpareObjects().size());
        assertEquals(
                TEST_STRING,
                testIVResponse.getResponseHeader().getOverallResponse().getSpareObjects().get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerOverallResponseSpareObjectsObjectMaxLenOK() {
        final String TEST_STRING = len10String.repeat(20);
        testIVResponse
                .getResponseHeader()
                .getOverallResponse()
                .setSpareObjects(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse.getResponseHeader().getOverallResponse().getSpareObjects().size());
        assertEquals(
                TEST_STRING,
                testIVResponse.getResponseHeader().getOverallResponse().getSpareObjects().get(0));
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerOverallResponseSpareObjectsObjectEmptyIsOK() {
        final String TEST_STRING = "";
        testIVResponse
                .getResponseHeader()
                .getOverallResponse()
                .setSpareObjects(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse.getResponseHeader().getOverallResponse().getSpareObjects().size());
        assertEquals(
                TEST_STRING,
                testIVResponse.getResponseHeader().getOverallResponse().getSpareObjects().get(0));
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerResponseCodeCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(2) + "1";
        testIVResponse.getResponseHeader().setResponseCode(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "ResponseCode" + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getResponseCode());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerResponseCodeMaxLenOK() {
        testIVResponse.getResponseHeader().setResponseCode(len10String.repeat(2));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        System.out.println(validationResult.getError());

        assertTrue(validationResult.isValid());
    }

    @Test
    void headerResponseCodeCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse.getResponseHeader().setResponseCode(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "ResponseCode" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getResponseCode());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerResponseTypeInfoIsOK() {
        final ResponseType type = ResponseType.INFO;
        testIVResponse.getResponseHeader().setResponseType(type);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(type, testIVResponse.getResponseHeader().getResponseType());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerResponseTypeNullIsFail() {
        final ResponseType type = null;
        testIVResponse.getResponseHeader().setResponseType(type);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                IdentityVerificationInfoResponseValidator.RESPONSE_TYPE_ERROR_MESSAGE + type;

        assertNull(testIVResponse.getResponseHeader().getResponseType());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerResponseTypeErrorIsFail() {
        final ResponseType type = ResponseType.ERROR;
        testIVResponse.getResponseHeader().setResponseType(type);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                IdentityVerificationInfoResponseValidator.RESPONSE_TYPE_ERROR_MESSAGE + type;

        assertEquals(type, testIVResponse.getResponseHeader().getResponseType());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerResponseTypeWarnIsFail() {
        final ResponseType type = ResponseType.WARN;
        testIVResponse.getResponseHeader().setResponseType(type);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                IdentityVerificationInfoResponseValidator.RESPONSE_TYPE_ERROR_MESSAGE + type;

        assertEquals(type, testIVResponse.getResponseHeader().getResponseType());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerResponseTypeWarningIsFail() {
        final ResponseType type = ResponseType.WARNING;
        testIVResponse.getResponseHeader().setResponseType(type);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                IdentityVerificationInfoResponseValidator.RESPONSE_TYPE_ERROR_MESSAGE + type;

        assertEquals(type, testIVResponse.getResponseHeader().getResponseType());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerResponseMessageCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(30) + "1";
        testIVResponse.getResponseHeader().setResponseMessage(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "ResponseMessage" + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getResponseMessage());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void headerResponseMessageMaxLenOK() {
        final String TEST_STRING = len10String.repeat(30);
        testIVResponse.getResponseHeader().setResponseMessage(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getResponseMessage());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void headerResponseMessageCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse.getResponseHeader().setResponseMessage(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "ResponseMessage" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(TEST_STRING, testIVResponse.getResponseHeader().getResponseMessage());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadCannotBeNULL() {
        testIVResponse.setClientResponsePayload(null);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                IdentityVerificationInfoResponseValidator
                        .NULL_CLIENT_RESPONSE_PAYLOAD_ERROR_MESSAGE;

        assertNull(testIVResponse.getClientResponsePayload());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsCannotBeNull() {
        testIVResponse.getClientResponsePayload().setOrchestrationDecisions(null);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecisions" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        assertNull(testIVResponse.getClientResponsePayload().getOrchestrationDecisions());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsCannotBeEmpty() {
        testIVResponse.getClientResponsePayload().setOrchestrationDecisions(new ArrayList<>());

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecisions" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                0, testIVResponse.getClientResponsePayload().getOrchestrationDecisions().size());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsCannotHaveANullElement() {
        testIVResponse.getClientResponsePayload().getOrchestrationDecisions().set(0, null);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                IdentityVerificationInfoResponseValidator.NULL_ORCHESTRATION_DECISION_ERROR_MESSAGE;

        assertNull(testIVResponse.getClientResponsePayload().getOrchestrationDecisions().get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionSequenceIdCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setSequenceId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:SequenceId"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getSequenceId());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionSequenceIdCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(4) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setSequenceId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:SequenceId"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getSequenceId());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionSequenceIdMaxLenOK() {
        final String TEST_STRING = len10String.repeat(4);
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setSequenceId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getSequenceId());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionSourceCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionSource(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:DecisionSource"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionSource());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionSourceCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(2) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionSource(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:DecisionSource"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionSource());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionSourceMaxLenOK() {
        final String TEST_STRING = len10String.repeat(2);
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionSource(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionSource());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionDecisionCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecision(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:Decision"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecision());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionDecisionCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(1) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecision(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:Decision"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecision());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionDecisionMaxLenOK() {
        final String TEST_STRING = len10String.repeat(1);
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecision(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecision());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsReasonsCannotBeNull() {
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionReasons(null);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:DecisionReasons"
                        + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        assertNull(
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionReasons());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsReasonsCannotBeEmpty() {
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionReasons(new ArrayList<>());

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:DecisionReasons"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                0,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionReasons()
                        .size());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsReasonsReasonCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionReasons(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:DecisionReasons:Reason"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionReasons()
                        .size());
        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionReasons()
                        .get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsReasonsReasonCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(9) + 1;
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionReasons(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:DecisionReasons:Reason"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionReasons()
                        .size());
        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionReasons()
                        .get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsReasonsReasonMaxLenOK() {
        final String TEST_STRING = len10String.repeat(9);
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionReasons(List.of(TEST_STRING));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionReasons()
                        .size());
        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionReasons()
                        .get(0));
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsScoreUnderMinFails() {
        final int TEST_VALUE = -1;
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String TEST_INTEGER_NAME = "OrchestrationDecision:Score";

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_VALUE,
                        IdentityVerificationInfoResponseValidator
                                .ORCHESTRATION_DECISION_SCORE_MIN_VALUE,
                        IdentityVerificationInfoResponseValidator
                                .ORCHESTRATION_DECISION_SCORE_MAX_VALUE,
                        TEST_INTEGER_NAME);

        assertEquals(
                TEST_VALUE,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getScore());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsMinOK() {
        final int TEST_VALUE = 0;
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_VALUE,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getScore());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOverMaxFails() {
        final int TEST_VALUE = 100000;
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String TEST_INTEGER_NAME = "OrchestrationDecision:Score";

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_VALUE,
                        IdentityVerificationInfoResponseValidator
                                .ORCHESTRATION_DECISION_SCORE_MIN_VALUE,
                        IdentityVerificationInfoResponseValidator
                                .ORCHESTRATION_DECISION_SCORE_MAX_VALUE,
                        TEST_INTEGER_NAME);

        assertEquals(
                TEST_VALUE,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getScore());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsMaxOK() {
        final int TEST_VALUE = 99999;
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_VALUE,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getScore());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionTextCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionText(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:DecisionText"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionText());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionTextCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(3) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionText(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:DecisionText"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionText());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionTextMaxLenOK() {
        final String TEST_STRING = len10String.repeat(3);
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionText(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionText());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationNextActionCannotBeEmpty() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setNextAction(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:NextAction"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getNextAction());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationNextActionCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(2) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setNextAction(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:NextAction"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getNextAction());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationNextActionMaxLenOK() {
        final String TEST_STRING = len10String.repeat(2);
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setNextAction(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getNextAction());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationAppReferenceNullIsAllowed() {
        final String TEST_STRING = null;
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setAppReference(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getAppReference());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationAppReferenceEmptyIsAllowed() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setAppReference(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getAppReference());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationAppReferenceCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(2) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setAppReference(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:AppReference"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getAppReference());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationAppReferenceMaxLenOK() {
        final String TEST_STRING = len10String.repeat(2);
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setAppReference(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getAppReference());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionTimeFailsIfEmpty() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionTime(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:DecisionTime"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionTime());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionTimeFailsIfInvalid() {
        final String TEST_STRING = "123";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionTime(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "OrchestrationDecision:DecisionTime"
                        + JsonValidationUtility.FAIL_PARSING_TIMESTAMP_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionTime());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadOrchestrationDecisionsOrchestrationDecisionTimeCanBeParsed() {
        final String TEST_STRING = "2022-01-01T00:00:01Z";
        testIVResponse
                .getClientResponsePayload()
                .getOrchestrationDecisions()
                .get(0)
                .setDecisionTime(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getOrchestrationDecisions()
                        .get(0)
                        .getDecisionTime());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsCannotBeNull() {
        testIVResponse.getClientResponsePayload().setDecisionElements(null);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElements" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        assertNull(testIVResponse.getClientResponsePayload().getDecisionElements());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsCannotBeEmpty() {
        testIVResponse.getClientResponsePayload().setDecisionElements(new ArrayList<>());

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElements" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(0, testIVResponse.getClientResponsePayload().getDecisionElements().size());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsCannotHaveANullElement() {
        testIVResponse.getClientResponsePayload().getDecisionElements().set(0, null);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                IdentityVerificationInfoResponseValidator.NULL_DECISION_ELEMENT_ERROR_MESSAGE;

        assertNull(testIVResponse.getClientResponsePayload().getDecisionElements().get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementApplicantIdEmptyIsFail() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setApplicantId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:ApplicantId" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getApplicantId());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementApplicantIdCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(4) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setApplicantId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:ApplicantId"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getApplicantId());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementApplicantIdMaxLenOK() {
        final String TEST_STRING = len10String.repeat(4);
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setApplicantId(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getApplicantId());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementServiceNameEmptyIsFail() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setServiceName(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:ServiceName" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getServiceName());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementServiceNameCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(4) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setServiceName(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:ServiceName"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getServiceName());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementServiceNameMaxLenOK() {
        final String TEST_STRING = len10String.repeat(4);
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setServiceName(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getServiceName());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementDecisionEmptyIsFail() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setDecision(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:Decision" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDecision());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementDecisionCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(2) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setDecision(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:Decision" + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDecision());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementDecisionMaxLenOK() {
        final String TEST_STRING = len10String.repeat(2);
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setDecision(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDecision());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementScoreUnderMinFails() {
        final int TEST_VALUE = -1;
        testIVResponse.getClientResponsePayload().getDecisionElements().get(0).setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String TEST_INTEGER_NAME = "DecisionElement:Score";

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_VALUE,
                        IdentityVerificationInfoResponseValidator.DECISION_ELEMENTS_SCORE_MIN_VALUE,
                        IdentityVerificationInfoResponseValidator.DECISION_ELEMENTS_SCORE_MAX_VALUE,
                        TEST_INTEGER_NAME);

        assertEquals(
                TEST_VALUE,
                testIVResponse.getClientResponsePayload().getDecisionElements().get(0).getScore());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementScoreMinOK() {
        final int TEST_VALUE = 0;
        testIVResponse.getClientResponsePayload().getDecisionElements().get(0).setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_VALUE,
                testIVResponse.getClientResponsePayload().getDecisionElements().get(0).getScore());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementScoreOverMaxFails() {
        final int TEST_VALUE = 261;
        testIVResponse.getClientResponsePayload().getDecisionElements().get(0).setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String TEST_INTEGER_NAME = "DecisionElement:Score";

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_VALUE,
                        IdentityVerificationInfoResponseValidator.DECISION_ELEMENTS_SCORE_MIN_VALUE,
                        IdentityVerificationInfoResponseValidator.DECISION_ELEMENTS_SCORE_MAX_VALUE,
                        TEST_INTEGER_NAME);

        assertEquals(
                TEST_VALUE,
                testIVResponse.getClientResponsePayload().getDecisionElements().get(0).getScore());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementScoreMaxOK() {
        final int TEST_VALUE = 90;
        testIVResponse.getClientResponsePayload().getDecisionElements().get(0).setScore(TEST_VALUE);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_VALUE,
                testIVResponse.getClientResponsePayload().getDecisionElements().get(0).getScore());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementDecisionTextEmptyIsOK() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setDecisionText(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDecisionText());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementDecisionTextCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(2) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setDecisionText(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:DecisionText"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDecisionText());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementDecisionTextMaxLenOK() {
        final String TEST_STRING = len10String.repeat(2);
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setDecisionText(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDecisionText());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementDecisionReasonEmptyIsFail() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setDecisionReason(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:DecisionReason"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDecisionReason());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementDecisionReasonCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(10) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setDecisionReason(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:DecisionReason"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDecisionReason());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementDecisionReasonMaxLenOK() {
        final String TEST_STRING = len10String.repeat(10);
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setDecisionReason(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getDecisionReason());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementAppReferenceEmptyIsFail() {
        final String TEST_STRING = "";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setAppReference(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:AppReference"
                        + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getAppReference());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementAppReferenceCannotBeTooLong() {
        final String TEST_STRING = len10String.repeat(10) + "1";
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setAppReference(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:AppReference"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getAppReference());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementAppReferenceMaxLenOK() {
        final String TEST_STRING = len10String.repeat(10);
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setAppReference(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                TEST_STRING,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getAppReference());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementRulesNullIsFail() {
        testIVResponse.getClientResponsePayload().getDecisionElements().get(0).setRules(null);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:Rules" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        assertNull(
                testIVResponse.getClientResponsePayload().getDecisionElements().get(0).getRules());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementRulesEmptyIsFail() {
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setRules(new ArrayList<>());

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:Rules" + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                0,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getRules()
                        .size());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementRuleScoreUnderMinFails() {
        final int TEST_VALUE = -1;

        final Rule testRule = new Rule();
        testRule.setRuleName("ScoreUnderMinFail");
        testRule.setRuleId("");
        testRule.setRuleScore(TEST_VALUE);
        testRule.setRuleText("Test");

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setRules(List.of(testRule));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String TEST_INTEGER_NAME = "DecisionElement:Rules:Rule:Score";

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_VALUE,
                        IdentityVerificationInfoResponseValidator
                                .DECISION_ELEMENTS_RULE_NAME_SERVICE_LEVEL_SCORE_MIN,
                        IdentityVerificationInfoResponseValidator
                                .DECISION_ELEMENTS_RULE_NAME_SERVICE_LEVEL_SCORE_MAX,
                        TEST_INTEGER_NAME);

        assertEquals(
                TEST_VALUE,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getRules()
                        .get(0)
                        .getRuleScore());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getRules()
                        .size());
        assertEquals(
                testRule,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getRules()
                        .get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementRuleScoreMinOK() {
        final int TEST_VALUE = 0;
        final Rule testRule = new Rule();
        testRule.setRuleName("ScoreUnderMinFail");
        testRule.setRuleId("");
        testRule.setRuleScore(TEST_VALUE);
        testRule.setRuleText("Test");

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setRules(List.of(testRule));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getRules()
                        .size());
        assertEquals(
                TEST_VALUE,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getRules()
                        .get(0)
                        .getRuleScore());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementRuleScoreOverMaxFails() {
        final int TEST_VALUE = 261;
        final Rule testRule = new Rule();
        testRule.setRuleName("ScoreUnderMinFail");
        testRule.setRuleId("");
        testRule.setRuleScore(TEST_VALUE);
        testRule.setRuleText("Test");

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setRules(List.of(testRule));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String TEST_INTEGER_NAME = "DecisionElement:Rules:Rule:Score";

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_VALUE,
                        IdentityVerificationInfoResponseValidator
                                .DECISION_ELEMENTS_RULE_NAME_SERVICE_LEVEL_SCORE_MIN,
                        IdentityVerificationInfoResponseValidator
                                .DECISION_ELEMENTS_RULE_NAME_SERVICE_LEVEL_SCORE_MAX,
                        TEST_INTEGER_NAME);

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getRules()
                        .size());
        assertEquals(
                TEST_VALUE,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getRules()
                        .get(0)
                        .getRuleScore());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementRuleScoreMaxOK() {
        final int TEST_VALUE = 260;
        final Rule testRule = new Rule();
        testRule.setRuleName("RuleScoreMaxOK");
        testRule.setRuleId("");
        testRule.setRuleScore(TEST_VALUE);
        testRule.setRuleText("Test");

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setRules(List.of(testRule));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getRules()
                        .size());
        assertEquals(
                testRule,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getRules()
                        .get(0));

        assertEquals(
                TEST_VALUE,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getRules()
                        .get(0)
                        .getRuleScore());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementWarningsErrorsNullIsFail() {
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(null);

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:WarningsErrors"
                        + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        assertNull(
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementWarningsErrorsEmptyIsOK() {
        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(new ArrayList<>());

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                0,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .size());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementWarningsErrorsResponseCodeMaxLenOK() {
        final WarningsErrors warningError = new WarningsErrors();
        warningError.setResponseCode(len10String.repeat(4));
        warningError.setResponseMessage("Message");
        warningError.setResponseType(ResponseType.INFO);

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(List.of(warningError));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .size());
        assertEquals(
                warningError,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .get(0));
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementWarningsErrorsResponseCodeCannotBeTooLong() {
        final WarningsErrors warningError = new WarningsErrors();
        warningError.setResponseCode(len10String.repeat(4) + "1");
        warningError.setResponseMessage("Message");
        warningError.setResponseType(ResponseType.INFO);

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(List.of(warningError));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:WarningsErrors:ResponseCode"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .size());
        assertEquals(
                warningError,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementWarningsErrorsResponseMessageMaxLenOK() {
        final WarningsErrors warningError = new WarningsErrors();
        warningError.setResponseCode("1");
        warningError.setResponseMessage(len10String.repeat(30));
        warningError.setResponseType(ResponseType.INFO);

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(List.of(warningError));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .size());
        assertEquals(
                warningError,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .get(0));
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void
            clientPayloadDecisionElementsDecisionElementWarningsErrorsResponseMessageCannotBeTooLong() {
        final WarningsErrors warningError = new WarningsErrors();
        warningError.setResponseCode("1");
        warningError.setResponseMessage(len10String.repeat(30) + "1");
        warningError.setResponseType(ResponseType.INFO);

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(List.of(warningError));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:WarningsErrors:ResponseMessage"
                        + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .size());
        assertEquals(
                warningError,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void
            clientPayloadDecisionElementsDecisionElementWarningsErrorsResponseTypeErrorIsFailWithSafeToLogWarningError() {
        WarningsErrors warningError = new WarningsErrors();
        warningError.setResponseCode("1");
        warningError.setResponseMessage("Message");
        warningError.setResponseType(ResponseType.ERROR);

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(List.of(warningError));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                "DecisionElement:ResponseType:Error, ResponseCode:"
                        + warningError.getResponseCode()
                        + ", ResponseMessage:"
                        + warningError.getResponseMessage();

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .size());
        assertEquals(
                warningError,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .get(0));
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void
            clientPayloadDecisionElementsDecisionElementWarningsErrorsResponseTypeErrorIsFailWithUnSafeToLogWarningErrorResponseCode() {
        WarningsErrors warningError = new WarningsErrors();
        final String CODE = len10String.repeat(4) + "1";
        warningError.setResponseCode(CODE);
        warningError.setResponseMessage("Message");
        warningError.setResponseType(ResponseType.ERROR);

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(List.of(warningError));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                IdentityVerificationInfoResponseValidator.WARNINGS_ERRORS_FALL_BACK_ERROR_MESAGE;

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .size());
        assertEquals(
                warningError,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .get(0));
        // Two errors 1 for the field and one for the Type
        assertEquals(2, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(1));
        assertFalse(validationResult.isValid());
    }

    @Test
    void
            clientPayloadDecisionElementsDecisionElementWarningsErrorsResponseTypeErrorIsFailWithUnSafeToLogWarningErrorResponseMessage() {
        WarningsErrors warningError = new WarningsErrors();
        final String MESSAGE = len10String.repeat(30) + "1";
        warningError.setResponseCode("1");
        warningError.setResponseMessage(MESSAGE);
        warningError.setResponseType(ResponseType.ERROR);

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(List.of(warningError));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        final String EXPECTED_ERROR =
                IdentityVerificationInfoResponseValidator.WARNINGS_ERRORS_FALL_BACK_ERROR_MESAGE;

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .size());
        assertEquals(
                warningError,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .get(0));
        // Two errors 1 for the field and one for the Type
        assertEquals(2, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(1));
        assertFalse(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementWarningsErrorsResponseTypeInfoIsOK() {
        final WarningsErrors warningError = new WarningsErrors();
        warningError.setResponseCode("1");
        warningError.setResponseMessage("Message");
        warningError.setResponseType(ResponseType.INFO);

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(List.of(warningError));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .size());
        assertEquals(
                warningError,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .get(0));
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementWarningsErrorsResponseTypeWarnIsOK() {
        WarningsErrors warningError = new WarningsErrors();
        warningError.setResponseCode("1");
        warningError.setResponseMessage("Message");
        warningError.setResponseType(ResponseType.WARN);

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(List.of(warningError));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .size());
        assertEquals(
                warningError,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .get(0));
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void clientPayloadDecisionElementsDecisionElementWarningsErrorsResponseTypeWarningIsOK() {
        WarningsErrors warningError = new WarningsErrors();
        warningError.setResponseCode("1");
        warningError.setResponseMessage("Message");
        warningError.setResponseType(ResponseType.WARNING);

        testIVResponse
                .getClientResponsePayload()
                .getDecisionElements()
                .get(0)
                .setWarningsErrors(List.of(warningError));

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validate(testIVResponse);

        assertEquals(
                1,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .size());
        assertEquals(
                warningError,
                testIVResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .get(0));
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }
}
