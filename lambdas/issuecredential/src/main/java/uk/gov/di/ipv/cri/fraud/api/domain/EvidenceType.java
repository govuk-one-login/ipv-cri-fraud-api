package uk.gov.di.ipv.cri.fraud.api.domain;

public enum EvidenceType {
    IDENTITY_CHECK("IdentityCheck");

    private final String name;

    private EvidenceType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
