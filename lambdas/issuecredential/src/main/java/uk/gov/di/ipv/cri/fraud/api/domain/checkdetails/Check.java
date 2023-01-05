package uk.gov.di.ipv.cri.fraud.api.domain.checkdetails;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"txn", "checkMethod", "fraudCheck"})
public class Check {

    @JsonProperty("fraudCheck")
    private String fraudCheck;

    @JsonProperty("checkMethod")
    private String checkMethod = "data";

    // Txn is required for IPR Check
    @JsonProperty("txn")
    private String txn;

    public Check(String fraudCheck) {
        this.fraudCheck = fraudCheck;
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
}
