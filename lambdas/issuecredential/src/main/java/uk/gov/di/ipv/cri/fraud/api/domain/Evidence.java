package uk.gov.di.ipv.cri.fraud.api.domain;

import java.util.List;

public class Evidence {
    private String txn;
    private EvidenceType type;
    private Integer identityFraudScore;
    private List<String> ci;

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    public EvidenceType getType() {
        return type;
    }

    public void setType(EvidenceType type) {
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
