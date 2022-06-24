package uk.gov.di.ipv.cri.fraud.api.domain.audit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TPREFraudAuditExtension {
    @JsonProperty("experianCrossCoreResponse")
    private final ExperianCrossCoreResponse experianCrossCoreResponse;

    public TPREFraudAuditExtension(List<String> uCodes) {
        this.experianCrossCoreResponse = new ExperianCrossCoreResponse(uCodes);
    }

    public ExperianCrossCoreResponse getExperianCrossCoreResponse() {
        return experianCrossCoreResponse;
    }
}
