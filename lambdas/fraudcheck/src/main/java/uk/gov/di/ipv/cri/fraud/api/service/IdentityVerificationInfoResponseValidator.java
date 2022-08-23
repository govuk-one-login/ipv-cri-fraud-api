package uk.gov.di.ipv.cri.fraud.api.service;

import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.*;
import uk.gov.di.ipv.cri.fraud.api.util.JsonValidationUtility;

import java.util.ArrayList;
import java.util.List;

public class IdentityVerificationInfoResponseValidator {
    public static final int HEADER_TENANT_ID_MAX_LEN = 30;
    public static final int HEADER_REQUEST_TYPE_MAX_LEN = 40;
    public static final int HEADER_CLIENT_REF_ID_MAX_LEN = 90;
    public static final int HEADER_EXP_REQUEST_ID_MAX_LEN = 90;
    public static final int OVERALL_RESPONSE_DECISION_MAX_LEN = 10;
    public static final int OVERALL_RESPONSE_DECISION_SCORE_MIN_VALUE = 0;
    public static final int OVERALL_RESPONSE_DECISION_SCORE_MAX_VALUE = 99999;
    public static final int OVERALL_RESPONSE_DECISION_TEXT_MAX_LEN = 30;
    public static final int OVERALL_RESPONSE_DECISION_REASONS_MAX_REASON_LEN = 90;
    public static final int OVERALL_RESPONSE_RECOMMENDED_NEXT_ACTIONS_NEXT_ACTION_MAX_LEN = 200;
    public static final int OVERALL_RESPONSE_RECOMMENDED_SPARE_OBJECTS_SPARE_OBJECT_MAX_LEN = 200;
    public static final int HEADER_RESPONSE_CODE_MAX_LEN = 20;
    public static final int HEADER_RESPONSE_MESSAGE_MAX_LEN = 300;
    public static final int ORCHESTRATION_DECISION_SEQUENCE_ID_MAX_LEN = 40;
    public static final int ORCHESTRATION_DECISION_SOURCE_MAX_LEN = 20;
    public static final int ORCHESTRATION_DECISION_DECISION_MAX_LEN = 10;
    public static final int ORCHESTRATION_DECISION_DECISION_REASONS_REASON_MAX_LEN = 90;
    public static final int ORCHESTRATION_DECISION_SCORE_MIN_VALUE = 0;
    public static final int ORCHESTRATION_DECISION_SCORE_MAX_VALUE = 99999;
    public static final int ORCHESTRATION_DECISION_DECISION_TEXT_MAX_LEN = 30;
    public static final int ORCHESTRATION_DECISION_NEXT_ACTION_MAX_LEN = 20;
    public static final int ORCHESTRATION_DECISION_APP_REFERENCE_MAX_LEN = 20;
    public static final int DECISION_ELEMENTS_APP_ID_MAX_LEN = 40;
    public static final int DECISION_ELEMENTS_SERVICE_NAME_MAX_LEN = 40;
    public static final int DECISION_ELEMENTS_DECISION_MAX_LEN = 20;
    public static final int DECISION_ELEMENTS_SCORE_MIN_VALUE = 0;
    public static final int DECISION_ELEMENTS_SCORE_MAX_VALUE = 99999;
    public static final int DECISION_ELEMENTS_DECISION_TEXT_MAX_LEN = 20;
    public static final int DECISION_ELEMENTS_DECISION_REASON_MAX_LEN = 100;
    public static final int DECISION_ELEMENTS_APP_REF_MAX_LEN = 100;
    public static final int DECISION_ELEMENTS_RULE_NAME_SERVICE_LEVEL_SCORE_MIN = 0;
    public static final int DECISION_ELEMENTS_RULE_NAME_SERVICE_LEVEL_SCORE_MAX = 99999;
    public static final int DECISION_ELEMENTS_WARNING_RESPONSE_CODE_MAX_LEN = 40;
    public static final int DECISION_ELEMENTS_WARNING_RESPONSE_MESSAGE_MAX_LEN = 300;

    public static final String DECISION_FIELD_NAME = "Decision";
    public static final String SCORE_FIELD_NAME = "Score";
    public static final String DECISION_TEXT_FIELD_NAME = "DecisionText";

    public static final String NULL_RESPONSE_ERROR_MESSAGE = "Response is null";
    public static final String NULL_HEADER_ERROR_MESSAGE = "Header not found.";

    public static final String NULL_ORCHESTRATION_DECISION_ERROR_MESSAGE =
            "Orchestration Decision is null.";
    public static final String NULL_DECISION_ELEMENT_ERROR_MESSAGE = "Decision Element is null.";

    public static final String NULL_HEADER_OVERALL_RESPONSE_ERROR_MESSAGE =
            "Header Overall response not found.";
    public static final String RESPONSE_TYPE_ERROR_MESSAGE =
            "Header Response Type INFO not found - ";
    public static final String NULL_CLIENT_RESPONSE_PAYLOAD_ERROR_MESSAGE =
            "ClientResponsePayload not found.";
    public static final String WARNINGS_ERRORS_FALL_BACK_ERROR_MESAGE =
            "DecisionElement:ResponseType:Error listed in WarningsErrors (Cannot log as values also had validation errors).";

    public ValidationResult<List<String>> validate(IdentityVerificationResponse response) {

        final List<String> validationErrors = new ArrayList<>();

        if (response != null) {
            validateIdentityVerificationResponseHeader(
                    response.getResponseHeader(), validationErrors, false);

            validateIdentityVerificationResponseClientResponsePayload(
                    response.getClientResponsePayload(), validationErrors, false);
        } else {
            validationErrors.add(NULL_RESPONSE_ERROR_MESSAGE);
        }

        return new ValidationResult<>(validationErrors.isEmpty(), validationErrors);
    }

    // TODO: will need to be reviewed in LIME-37
    public ValidationResult<List<String>> validatePEP(PEPResponse response) {

        final List<String> validationErrors = new ArrayList<>();

        if (response != null) {
            validateIdentityVerificationResponseHeader(
                    response.getResponseHeader(), validationErrors, true);

            validateIdentityVerificationResponseClientResponsePayload(
                    response.getClientResponsePayload(), validationErrors, true);
        } else {
            validationErrors.add(NULL_RESPONSE_ERROR_MESSAGE);
        }

        return new ValidationResult<>(validationErrors.isEmpty(), validationErrors);
    }

    private void validateIdentityVerificationResponseHeader(
            ResponseHeader header, final List<String> validationErrors, boolean pepEnabled) {
        if (header == null) {
            validationErrors.add(NULL_HEADER_ERROR_MESSAGE);
            return;
        }

        JsonValidationUtility.validateStringDataEmptyIsFail(
                header.getTenantID(), HEADER_TENANT_ID_MAX_LEN, "TenantID", validationErrors);

        JsonValidationUtility.validateStringDataEmptyIsFail(
                header.getRequestType(),
                HEADER_REQUEST_TYPE_MAX_LEN,
                "RequestType",
                validationErrors);

        JsonValidationUtility.validateStringDataEmptyIsFail(
                header.getClientReferenceId(),
                HEADER_CLIENT_REF_ID_MAX_LEN,
                "ClientReferenceId",
                validationErrors);

        JsonValidationUtility.validateStringDataEmptyIsFail(
                header.getExpRequestId(),
                HEADER_EXP_REQUEST_ID_MAX_LEN,
                "ExpRequestId",
                validationErrors);

        JsonValidationUtility.validateTimeStampData(
                header.getMessageTime(), "MessageTime", validationErrors);

        validateIdentityVerificationResponseHeaderOverallResponse(
                header.getOverallResponse(), validationErrors, pepEnabled);

        JsonValidationUtility.validateStringDataEmptyIsFail(
                header.getResponseCode(),
                HEADER_RESPONSE_CODE_MAX_LEN,
                "ResponseCode",
                validationErrors);

        // Validation is based on the assumption that we are validating an info response.
        if (header.getResponseType() != ResponseType.INFO) {
            validationErrors.add(RESPONSE_TYPE_ERROR_MESSAGE + header.getResponseType());
        }

        JsonValidationUtility.validateStringDataEmptyIsFail(
                header.getResponseMessage(),
                HEADER_RESPONSE_MESSAGE_MAX_LEN,
                "ResponseMessage",
                validationErrors);
    }

    private void validateIdentityVerificationResponseHeaderOverallResponse(
            OverallResponse overallResponse,
            final List<String> validationErrors,
            boolean pepEnabled) {
        if (overallResponse != null) {
            String subObjectName = "OverallResponse:";

            JsonValidationUtility.validateStringDataEmptyIsFail(
                    overallResponse.getDecision(),
                    OVERALL_RESPONSE_DECISION_MAX_LEN,
                    subObjectName + DECISION_FIELD_NAME,
                    validationErrors);

            JsonValidationUtility.validateIntegerRangeData(
                    overallResponse.getScore(),
                    OVERALL_RESPONSE_DECISION_SCORE_MIN_VALUE,
                    OVERALL_RESPONSE_DECISION_SCORE_MAX_VALUE,
                    subObjectName + SCORE_FIELD_NAME,
                    validationErrors);

            JsonValidationUtility.validateStringDataEmptyIsFail(
                    overallResponse.getDecisionText(),
                    OVERALL_RESPONSE_DECISION_TEXT_MAX_LEN,
                    subObjectName + DECISION_TEXT_FIELD_NAME,
                    validationErrors);

            if (!pepEnabled) {
                if (JsonValidationUtility.validateListDataEmptyIsFail(
                        overallResponse.getDecisionReasons(),
                        subObjectName + "DecisionReasons",
                        validationErrors)) {
                    for (String reason : overallResponse.getDecisionReasons()) {
                        JsonValidationUtility.validateStringDataEmptyIsFail(
                                reason,
                                OVERALL_RESPONSE_DECISION_REASONS_MAX_REASON_LEN,
                                subObjectName + "DecisionReasons:Reason",
                                validationErrors);
                    }
                }
            }

            if (JsonValidationUtility.validateListDataEmptyIsAllowed(
                    overallResponse.getRecommendedNextActions(),
                    "RecommendedNextActions",
                    validationErrors)) {
                for (String nextAction : overallResponse.getRecommendedNextActions()) {
                    JsonValidationUtility.validateStringDataEmptyIsAllowed(
                            nextAction,
                            OVERALL_RESPONSE_RECOMMENDED_NEXT_ACTIONS_NEXT_ACTION_MAX_LEN,
                            subObjectName + "RecommendedNextActions:Action",
                            validationErrors);
                }
            }

            if (JsonValidationUtility.validateListDataEmptyIsAllowed(
                    overallResponse.getSpareObjects(), "SpareObjects", validationErrors)) {
                for (String spareObject : overallResponse.getSpareObjects()) {
                    JsonValidationUtility.validateStringDataEmptyIsAllowed(
                            spareObject,
                            OVERALL_RESPONSE_RECOMMENDED_SPARE_OBJECTS_SPARE_OBJECT_MAX_LEN,
                            subObjectName + "SpareObjects:Object",
                            validationErrors);
                }
            }
        } else {
            validationErrors.add(NULL_HEADER_OVERALL_RESPONSE_ERROR_MESSAGE);
        }
    }

    private void validateIdentityVerificationResponseClientResponsePayload(
            ClientResponsePayload payload,
            final List<String> validationErrors,
            boolean pepEnabled) {

        if (payload == null) {
            validationErrors.add(NULL_CLIENT_RESPONSE_PAYLOAD_ERROR_MESSAGE);
            return;
        }

        List<OrchestrationDecision> orchestrationDecisions = payload.getOrchestrationDecisions();
        if (JsonValidationUtility.validateListDataEmptyIsFail(
                orchestrationDecisions, "OrchestrationDecisions", validationErrors)) {
            for (OrchestrationDecision orchestrationDecision : orchestrationDecisions) {
                validateIdentityVerificationResponseClientResponsePayloadOrchestrationDecision(
                        orchestrationDecision, validationErrors);
            }
        }

        List<DecisionElement> decisionElements = payload.getDecisionElements();
        if (JsonValidationUtility.validateListDataEmptyIsFail(
                decisionElements, "DecisionElements", validationErrors)) {
            for (DecisionElement decisionElement : decisionElements) {
                validateIdentityVerificationResponseClientResponsePayloadDecisionElement(
                        decisionElement, validationErrors, pepEnabled);
            }
        }
    }

    private void validateIdentityVerificationResponseClientResponsePayloadOrchestrationDecision(
            OrchestrationDecision orchestrationDecision, List<String> validationErrors) {
        if (orchestrationDecision == null) {
            validationErrors.add(NULL_ORCHESTRATION_DECISION_ERROR_MESSAGE);
            return;
        }

        String subObjectName = "OrchestrationDecision:";

        JsonValidationUtility.validateStringDataEmptyIsFail(
                orchestrationDecision.getSequenceId(),
                ORCHESTRATION_DECISION_SEQUENCE_ID_MAX_LEN,
                subObjectName + "SequenceId",
                validationErrors);

        JsonValidationUtility.validateStringDataEmptyIsFail(
                orchestrationDecision.getDecisionSource(),
                ORCHESTRATION_DECISION_SOURCE_MAX_LEN,
                subObjectName + "DecisionSource",
                validationErrors);

        JsonValidationUtility.validateStringDataEmptyIsFail(
                orchestrationDecision.getDecision(),
                ORCHESTRATION_DECISION_DECISION_MAX_LEN,
                subObjectName + DECISION_FIELD_NAME,
                validationErrors);

        if (JsonValidationUtility.validateListDataEmptyIsFail(
                orchestrationDecision.getDecisionReasons(),
                subObjectName + "DecisionReasons",
                validationErrors)) {
            for (String reason : orchestrationDecision.getDecisionReasons()) {
                JsonValidationUtility.validateStringDataEmptyIsFail(
                        reason,
                        ORCHESTRATION_DECISION_DECISION_REASONS_REASON_MAX_LEN,
                        subObjectName + "DecisionReasons:Reason",
                        validationErrors);
            }
        }

        JsonValidationUtility.validateIntegerRangeData(
                orchestrationDecision.getScore(),
                ORCHESTRATION_DECISION_SCORE_MIN_VALUE,
                ORCHESTRATION_DECISION_SCORE_MAX_VALUE,
                subObjectName + SCORE_FIELD_NAME,
                validationErrors);

        JsonValidationUtility.validateStringDataEmptyIsFail(
                orchestrationDecision.getDecisionText(),
                ORCHESTRATION_DECISION_DECISION_TEXT_MAX_LEN,
                subObjectName + DECISION_TEXT_FIELD_NAME,
                validationErrors);

        JsonValidationUtility.validateStringDataEmptyIsFail(
                orchestrationDecision.getNextAction(),
                ORCHESTRATION_DECISION_NEXT_ACTION_MAX_LEN,
                subObjectName + "NextAction",
                validationErrors);

        JsonValidationUtility.validateStringDataNullAndEmptyIsAllowed(
                orchestrationDecision.getAppReference(),
                ORCHESTRATION_DECISION_APP_REFERENCE_MAX_LEN,
                subObjectName + "AppReference",
                validationErrors);

        JsonValidationUtility.validateTimeStampData(
                orchestrationDecision.getDecisionTime(),
                subObjectName + "DecisionTime",
                validationErrors);
    }

    private void validateIdentityVerificationResponseClientResponsePayloadDecisionElement(
            DecisionElement decisionElement,
            final List<String> validationErrors,
            boolean pepEnabled) {
        if (decisionElement == null) {
            validationErrors.add(NULL_DECISION_ELEMENT_ERROR_MESSAGE);
            return;
        }

        String subObjectName = "DecisionElement:";
        if (!pepEnabled) {
            JsonValidationUtility.validateStringDataEmptyIsFail(
                    decisionElement.getApplicantId(),
                    DECISION_ELEMENTS_APP_ID_MAX_LEN,
                    subObjectName + "ApplicantId",
                    validationErrors);

            JsonValidationUtility.validateStringDataEmptyIsFail(
                    decisionElement.getDecision(),
                    DECISION_ELEMENTS_DECISION_MAX_LEN,
                    subObjectName + DECISION_FIELD_NAME,
                    validationErrors);

            JsonValidationUtility.validateStringDataEmptyIsFail(
                    decisionElement.getDecisionReason(),
                    DECISION_ELEMENTS_DECISION_REASON_MAX_LEN,
                    subObjectName + "DecisionReason",
                    validationErrors);

            List<Rule> rules = decisionElement.getRules();
            if (JsonValidationUtility.validateListDataEmptyIsFail(
                    rules, subObjectName + "Rules", validationErrors)) {
                for (Rule rule : rules) {
                    placeholderValidateDecisionElementRule(rule, validationErrors);
                }
            }
        }

        JsonValidationUtility.validateStringDataEmptyIsFail(
                decisionElement.getServiceName(),
                DECISION_ELEMENTS_SERVICE_NAME_MAX_LEN,
                subObjectName + "ServiceName",
                validationErrors);

        JsonValidationUtility.validateIntegerRangeData(
                decisionElement.getScore(),
                DECISION_ELEMENTS_SCORE_MIN_VALUE,
                DECISION_ELEMENTS_SCORE_MAX_VALUE,
                subObjectName + SCORE_FIELD_NAME,
                validationErrors);

        JsonValidationUtility.validateStringDataNullAndEmptyIsAllowed(
                decisionElement.getDecisionText(),
                DECISION_ELEMENTS_DECISION_TEXT_MAX_LEN,
                subObjectName + DECISION_TEXT_FIELD_NAME,
                validationErrors);

        JsonValidationUtility.validateStringDataEmptyIsFail(
                decisionElement.getAppReference(),
                DECISION_ELEMENTS_APP_REF_MAX_LEN,
                subObjectName + "AppReference",
                validationErrors);

        validateIdentityVerificationResponseClientResponsePayloadDecisionElementWarningsErrors(
                decisionElement.getWarningsErrors(), validationErrors);
    }

    /*
     * Placeholder for stricter validation of DecisionElementRules
     */
    private void placeholderValidateDecisionElementRule(
            Rule rule, final List<String> validationErrors) {

        String subObjectName = "DecisionElement:Rules:Rule:";

        if (rule.getRuleScore() != null) {
            JsonValidationUtility.validateIntegerRangeData(
                    rule.getRuleScore(),
                    DECISION_ELEMENTS_RULE_NAME_SERVICE_LEVEL_SCORE_MIN,
                    DECISION_ELEMENTS_RULE_NAME_SERVICE_LEVEL_SCORE_MAX,
                    subObjectName + SCORE_FIELD_NAME,
                    validationErrors);
        }
    }

    private void
            validateIdentityVerificationResponseClientResponsePayloadDecisionElementWarningsErrors(
                    List<WarningsErrors> warningsErrors, List<String> validationErrors) {
        if (JsonValidationUtility.validateListDataEmptyIsAllowed(
                warningsErrors, "DecisionElement:WarningsErrors", validationErrors)) {
            for (WarningsErrors warningError : warningsErrors) {

                boolean warningErrorSafeToLog = true;

                if (warningError.getResponseCode().length()
                        > DECISION_ELEMENTS_WARNING_RESPONSE_CODE_MAX_LEN) {
                    validationErrors.add(
                            "DecisionElement:WarningsErrors:ResponseCode is too long.");

                    warningErrorSafeToLog = false;
                }
                if (warningError.getResponseMessage().length()
                        > DECISION_ELEMENTS_WARNING_RESPONSE_MESSAGE_MAX_LEN) {
                    validationErrors.add(
                            "DecisionElement:WarningsErrors:ResponseMessage is too long.");
                    warningErrorSafeToLog = false;
                }

                // Only fail for ERROR responses
                if (warningError.getResponseType() == ResponseType.ERROR) {
                    if (warningErrorSafeToLog) {
                        validationErrors.add(
                                "DecisionElement:ResponseType:Error, ResponseCode:"
                                        + warningError.getResponseCode()
                                        + ", ResponseMessage:"
                                        + warningError.getResponseMessage());
                    } else {
                        validationErrors.add(WARNINGS_ERRORS_FALL_BACK_ERROR_MESAGE);
                    }
                }
            }
        }
    }
}
