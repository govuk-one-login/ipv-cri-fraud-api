package uk.gov.di.ipv.cri.fraud.api.domain.audit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class TPREFraudAuditExtension {
    @JsonProperty("experianCrossCoreResponse")
    private final ExperianCrossCoreResponse experianCrossCoreResponse;

    public TPREFraudAuditExtension(List<String> uCodes) {
        this.experianCrossCoreResponse = new ExperianCrossCoreResponse(uCodes);
    }

    public ExperianCrossCoreResponse getExperianCrossCoreResponse() {
        return experianCrossCoreResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TPREFraudAuditExtension that = (TPREFraudAuditExtension) o;
        return Objects.equals(experianCrossCoreResponse, that.experianCrossCoreResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(experianCrossCoreResponse);
    }
}
