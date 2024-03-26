package uk.gov.di.ipv.cri.fraud.library.domain;

public class Thumbprints {

    private final String sha1Thumbprint;
    private final String sha256Thumbprint;

    public Thumbprints(String sha1Thumbprint, String sha256Thumbprint) {
        this.sha1Thumbprint = sha1Thumbprint;
        this.sha256Thumbprint = sha256Thumbprint;
    }

    public String getSha1Thumbprint() {
        return sha1Thumbprint;
    }

    public String getSha256Thumbprint() {
        return sha256Thumbprint;
    }
}
