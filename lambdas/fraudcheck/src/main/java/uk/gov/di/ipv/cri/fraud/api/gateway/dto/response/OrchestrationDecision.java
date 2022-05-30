package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrchestrationDecision {

    @JsonProperty("sequenceId")
    private String sequenceId;

    @JsonProperty("decisionSource")
    private String decisionSource;

    @JsonProperty("decision")
    private String decision;

    @JsonProperty("decisionReasons")
    private List<String> decisionReasons = new ArrayList<>();

    @JsonProperty("score")
    private Integer score;

    @JsonProperty("decisionText")
    private String decisionText;

    @JsonProperty("nextAction")
    private String nextAction;

    @JsonProperty("decisionTime")
    private String decisionTime;

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getDecisionSource() {
        return decisionSource;
    }

    public void setDecisionSource(String decisionSource) {
        this.decisionSource = decisionSource;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public List<String> getDecisionReasons() {
        return decisionReasons;
    }

    public void setDecisionReasons(List<String> decisionReasons) {
        this.decisionReasons = decisionReasons;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getDecisionText() {
        return decisionText;
    }

    public void setDecisionText(String decisionText) {
        this.decisionText = decisionText;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public String getDecisionTime() {
        return decisionTime;
    }

    public void setDecisionTime(String decisionTime) {
        this.decisionTime = decisionTime;
    }
}
