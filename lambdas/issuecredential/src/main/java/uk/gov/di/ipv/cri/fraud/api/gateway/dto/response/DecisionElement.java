package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionElement {

    @JsonProperty("serviceName")
    private String serviceName;

    @JsonProperty("applicantId")
    private String applicantId;

    @JsonProperty("decision")
    private String decision;

    @JsonProperty("score")
    private Integer score;

    @JsonProperty("decisionText")
    private String decisionText;

    @JsonProperty("decisionReason")
    private String decisionReason;

    @JsonProperty("appReference")
    private String appReference;

    @JsonProperty("rules")
    private List<Rule> rules = new ArrayList<>();

    @JsonProperty("warningsErrors")
    private List<Object> warningsErrors = new ArrayList<>();

    @JsonProperty("otherData")
    private OtherData otherData;

    @JsonProperty("matches")
    private List<Match> matches = new ArrayList<>();

    @JsonProperty("dataCounts")
    private List<DataCount> dataCounts = new ArrayList<>();

    @JsonProperty("scores")
    private List<Score> scores = new ArrayList<>();

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
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

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public String getAppReference() {
        return appReference;
    }

    public void setAppReference(String appReference) {
        this.appReference = appReference;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Object> getWarningsErrors() {
        return warningsErrors;
    }

    public void setWarningsErrors(List<Object> warningsErrors) {
        this.warningsErrors = warningsErrors;
    }

    public OtherData getOtherData() {
        return otherData;
    }

    public void setOtherData(OtherData otherData) {
        this.otherData = otherData;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public List<DataCount> getDataCounts() {
        return dataCounts;
    }

    public void setDataCounts(List<DataCount> dataCounts) {
        this.dataCounts = dataCounts;
    }

    public List<Score> getScores() {
        return scores;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }
}
