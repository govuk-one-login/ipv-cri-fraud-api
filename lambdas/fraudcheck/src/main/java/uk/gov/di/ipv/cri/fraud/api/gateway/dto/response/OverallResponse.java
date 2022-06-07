package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OverallResponse {

    @JsonProperty("decision")
    private String decision; // ACCEPT, REJECT or REFER

    @JsonProperty("decisionText")
    private String decisionText;

    @JsonProperty("decisionReasons")
    private List<String> decisionReasons = new ArrayList<>();

    @JsonProperty("recommendedNextActions")
    private List<Object> recommendedNextActions = new ArrayList<>();

    @JsonProperty("spareObjects")
    private List<Object> spareObjects = new ArrayList<>();

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getDecisionText() {
        return decisionText;
    }

    public void setDecisionText(String decisionText) {
        this.decisionText = decisionText;
    }

    public List<String> getDecisionReasons() {
        return decisionReasons;
    }

    public void setDecisionReasons(List<String> decisionReasons) {
        this.decisionReasons = decisionReasons;
    }

    public List<Object> getRecommendedNextActions() {
        return recommendedNextActions;
    }

    public void setRecommendedNextActions(List<Object> recommendedNextActions) {
        this.recommendedNextActions = recommendedNextActions;
    }

    public List<Object> getSpareObjects() {
        return spareObjects;
    }

    public void setSpareObjects(List<Object> spareObjects) {
        this.spareObjects = spareObjects;
    }
}
