package uk.gov.di.ipv.cri.fraud.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.di.ipv.cri.fraud.api.domain.checkdetails.Check;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "txn", "identityFraudScore", "ci"})
public class Evidence {
    @JsonProperty("type")
    private String type;

    @JsonProperty("txn")
    private String txn;

    @JsonProperty("identityFraudScore")
    private Integer identityFraudScore;

    @JsonProperty("activityHistoryScore")
    private Integer activityHistoryScore;

    @JsonProperty("ci")
    private List<String> ci;

    @JsonProperty("decisionScore")
    private String decisionScore;

    @JsonProperty("checkDetails")
    private List<Check> checkDetails;

    @JsonProperty("failedCheckDetails")
    private List<Check> failedCheckDetails;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    public Integer getIdentityFraudScore() {
        return identityFraudScore;
    }

    public void setIdentityFraudScore(Integer identityFraudScore) {
        this.identityFraudScore = identityFraudScore;
    }

    public List<String> getCi() {
        return ci;
    }

    public void setCi(List<String> ci) {
        this.ci = ci;
    }

    public String getDecisionScore() {
        return decisionScore;
    }

    public void setDecisionScore(String decisionScore) {
        this.decisionScore = decisionScore;
    }

    public List<Check> getCheckDetails() {
        return checkDetails;
    }

    public void setCheckDetails(List<Check> checkDetails) {
        this.checkDetails = checkDetails;
    }

    public List<Check> getFailedCheckDetails() {
        return failedCheckDetails;
    }

    public void setFailedCheckDetails(List<Check> failedCheckDetails) {
        this.failedCheckDetails = failedCheckDetails;
    }

    public Integer getActivityHistoryScore() {
        return activityHistoryScore;
    }

    public void setActivityHistoryScore(Integer activityHistoryScore) {
        this.activityHistoryScore = activityHistoryScore;
    }
}
