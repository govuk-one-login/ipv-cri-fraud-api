package uk.gov.di.ipv.cri.fraud.api.domain.checkdetails;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.di.ipv.cri.fraud.library.domain.CheckType;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"txn", "checkMethod", "fraudCheck"})
public class Check {

    @JsonProperty("fraudCheck")
    private String fraudCheck;

    @JsonProperty("checkMethod")
    private String checkMethod = "data";

    @JsonProperty("activityFrom")
    private String activityFrom;

    // Txn is required for IPR Check
    @JsonProperty("txn")
    private String txn;

    @JsonProperty("identityCheckPolicy")
    private String identityCheckPolicy;

    public Check(CheckType checkType) {
        this.fraudCheck = checkType.toString().toLowerCase();
    }

    public String getFraudCheck() {
        return fraudCheck;
    }

    public String getCheckMethod() {
        return checkMethod;
    }

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    public String getActivityFrom() {
        return activityFrom;
    }

    public void setActivityFrom(String activityFrom) {
        this.activityFrom = activityFrom;
    }

    public String getIdentityCheckPolicy() {
        return identityCheckPolicy;
    }

    public void setIdentityCheckPolicy(String identityCheckPolicy) {
        this.identityCheckPolicy = identityCheckPolicy;
    }

    public void clearFraudCheckField() {
        fraudCheck = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Check check = (Check) o;
        return Objects.equals(fraudCheck, check.fraudCheck)
                && Objects.equals(checkMethod, check.checkMethod)
                && Objects.equals(activityFrom, check.activityFrom)
                && Objects.equals(txn, check.txn)
                && Objects.equals(identityCheckPolicy, check.identityCheckPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fraudCheck, checkMethod, activityFrom, txn, identityCheckPolicy);
    }
}
