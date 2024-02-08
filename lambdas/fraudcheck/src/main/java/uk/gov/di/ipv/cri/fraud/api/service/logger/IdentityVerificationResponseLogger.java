package uk.gov.di.ipv.cri.fraud.api.service.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.ClientResponsePayload;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.DecisionElement;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.OrchestrationDecision;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.OverallResponse;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.ResponseHeader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IdentityVerificationResponseLogger {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String DECISION_KEY = "decision";
    private static final String SCORE_KEY = "score";

    public static final String NULL_RESPONSE = "response was null";

    private final ObjectMapper objectMapper;

    /** This class is intended log only with no impact on control flow. */
    public IdentityVerificationResponseLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Logs the fields and values from the deserialized response objects, outputting the log line in
     * the same format as the original json response. Uses string/object maps to log only the field
     * we want extracted. This avoids any changes to response objects as we want to ensure any
     * missing fields/nulls values are logged also.
     */
    public void logResponseFields(IdentityVerificationResponse response) {

        Map<String, Object> responseMap = new LinkedHashMap<>();

        if (null != response) {

            Map<String, Object> responseHeaderMap =
                    captureHeaderFields(response.getResponseHeader());
            responseMap.put("responseHeader", responseHeaderMap);

            Map<String, Object> clientResponsePayloadMap =
                    captureClientResponsePayloadFields(response.getClientResponsePayload());
            responseMap.put("clientResponsePayload", clientResponsePayloadMap);

            // Caught and logged here, as this class must have no impact on the CRI's operation.
            try {
                String json = objectMapper.writeValueAsString(responseMap);

                LOGGER.info("Extracted response fields - {}", json);
            } catch (JsonProcessingException e) {
                LOGGER.error("Error extracting response fields - {}", e.getMessage());
            }
        } else {
            LOGGER.error(NULL_RESPONSE);
        }
    }

    /////////////////////////////////////////////
    /// Response Header                       ///
    /////////////////////////////////////////////

    private Map<String, Object> captureHeaderFields(ResponseHeader responseHeader) {

        Map<String, Object> headerFieldMap = new LinkedHashMap<>();

        if (null != responseHeader) {
            headerFieldMap.put("requestType", responseHeader.getRequestType());
            headerFieldMap.put("expRequestId", responseHeader.getExpRequestId());

            Map<String, Object> overallResponseMap =
                    captureHeaderOverallResponseFields(responseHeader.getOverallResponse());
            headerFieldMap.put("overallResponse", overallResponseMap);

            headerFieldMap.put("responseCode", responseHeader.getResponseCode());
            headerFieldMap.put("responseType", responseHeader.getResponseType());
        }

        return headerFieldMap;
    }

    private Map<String, Object> captureHeaderOverallResponseFields(
            OverallResponse overallResponse) {

        Map<String, Object> overallResponseMap = new LinkedHashMap<>();

        if (null != overallResponse) {
            overallResponseMap.put(DECISION_KEY, overallResponse.getDecision());
            overallResponseMap.put("decisionText", overallResponse.getDecisionText());
            overallResponseMap.put("decisionReasons", overallResponse.getDecisionReasons());
        }

        return overallResponseMap;
    }

    /////////////////////////////////////////////
    /// ClientResponsePayload                 ///
    /////////////////////////////////////////////

    private Map<String, Object> captureClientResponsePayloadFields(
            ClientResponsePayload clientResponsePayload) {

        Map<String, Object> clientResponsePayloadFieldMap = new LinkedHashMap<>();

        if (null != clientResponsePayload) {
            List<Map<String, Object>> orchestrationDecisionsMapValue =
                    captureOrchestrationDecisionsFields(
                            clientResponsePayload.getOrchestrationDecisions());

            clientResponsePayloadFieldMap.put(
                    "orchestrationDecisions", orchestrationDecisionsMapValue);

            List<Map<String, Object>> decisionElementsMapValue =
                    captureDecisionElementsFields(clientResponsePayload.getDecisionElements());

            clientResponsePayloadFieldMap.put("decisionElements", decisionElementsMapValue);
        }

        return clientResponsePayloadFieldMap;
    }

    /////////////////////////////////////////////
    /// OrchestrationDecisions                ///
    /////////////////////////////////////////////

    private List<Map<String, Object>> captureOrchestrationDecisionsFields(
            List<OrchestrationDecision> orchestrationDecisions) {

        List<Map<String, Object>> orchestrationDecisionsListValue = new ArrayList<>();

        if (null != orchestrationDecisions) {

            for (OrchestrationDecision orchestrationDecision : orchestrationDecisions) {

                Map<String, Object> orchestrationDecisionMap = new LinkedHashMap<>();

                if (null != orchestrationDecision) {
                    orchestrationDecisionMap.put(
                            "decisionSource", orchestrationDecision.getDecisionSource());
                    orchestrationDecisionMap.put(DECISION_KEY, orchestrationDecision.getDecision());
                    orchestrationDecisionMap.put(
                            "decisionReasons", orchestrationDecision.getDecisionReasons());
                    orchestrationDecisionMap.put(SCORE_KEY, orchestrationDecision.getScore());
                    orchestrationDecisionMap.put(
                            "decisionText", orchestrationDecision.getDecisionText());
                }

                orchestrationDecisionsListValue.add(orchestrationDecisionMap);
            }
        }

        return orchestrationDecisionsListValue;
    }

    /////////////////////////////////////////////
    /// DecisionElements                      ///
    /////////////////////////////////////////////

    private List<Map<String, Object>> captureDecisionElementsFields(
            List<DecisionElement> decisionElements) {

        List<Map<String, Object>> decisionElementsListValue = new ArrayList<>();

        if (null != decisionElements) {

            for (DecisionElement decisionElement : decisionElements) {

                Map<String, Object> decisionElementMap = new LinkedHashMap<>();

                if (null != decisionElement) {
                    decisionElementMap.put(DECISION_KEY, decisionElement.getDecision());
                    decisionElementMap.put(SCORE_KEY, decisionElement.getScore());
                    decisionElementMap.put("decisionReason", decisionElement.getDecisionReason());
                }

                decisionElementsListValue.add(decisionElementMap);
            }
        }

        return decisionElementsListValue;
    }
}
