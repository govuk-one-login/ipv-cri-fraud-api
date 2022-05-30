package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientResponsePayload {

    @JsonProperty("orchestrationDecisions")
    private List<OrchestrationDecision> orchestrationDecisions = new ArrayList<>();

    @JsonProperty("decisionElements")
    private List<DecisionElement> decisionElements = new ArrayList<>();

    public List<OrchestrationDecision> getOrchestrationDecisions() {
        return orchestrationDecisions;
    }

    public void setOrchestrationDecisions(List<OrchestrationDecision> orchestrationDecisions) {
        this.orchestrationDecisions = orchestrationDecisions;
    }

    public List<DecisionElement> getDecisionElements() {
        return decisionElements;
    }

    public void setDecisionElements(List<DecisionElement> decisionElements) {
        this.decisionElements = decisionElements;
    }
}
