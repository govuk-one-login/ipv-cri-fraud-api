package uk.gov.di.ipv.cri.fraud.api.domain.audit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class ExperianCrossCoreResponse {

    @JsonProperty("uCodes")
    private final List<String> uCodes;

    public ExperianCrossCoreResponse(List<String> uCodes) {
        this.uCodes = uCodes;
    }

    public List<String> getuCodes() {
        return uCodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperianCrossCoreResponse that = (ExperianCrossCoreResponse) o;
        return Objects.equals(uCodes, that.uCodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uCodes);
    }
}
