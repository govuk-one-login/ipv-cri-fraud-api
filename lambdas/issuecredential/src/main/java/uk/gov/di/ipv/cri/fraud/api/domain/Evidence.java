package uk.gov.di.ipv.cri.fraud.api.domain;

import java.util.List;

public class Evidence {
    private String txn;
    private String type;
    private Integer identityFraudScore;
    private List<String> ci;

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
