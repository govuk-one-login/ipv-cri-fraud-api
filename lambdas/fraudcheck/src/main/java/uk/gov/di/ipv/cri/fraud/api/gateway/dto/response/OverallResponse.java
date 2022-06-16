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

    @JsonProperty("score")
    private int score;

    @JsonProperty("decisionReasons")
    private List<String> decisionReasons = new ArrayList<>();

    @JsonProperty("recommendedNextActions")
    private List<String> recommendedNextActions = new ArrayList<>();

    @JsonProperty("spareObjects")
    private List<String> spareObjects = new ArrayList<>();

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getDecisionText() {
        return decisionText;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
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

    public List<String> getRecommendedNextActions() {
        return recommendedNextActions;
    }

    public void setRecommendedNextActions(List<String> recommendedNextActions) {
        this.recommendedNextActions = recommendedNextActions;
    }

    public List<String> getSpareObjects() {
        return spareObjects;
    }

    public void setSpareObjects(List<String> spareObjects) {
        this.spareObjects = spareObjects;
    }
}
