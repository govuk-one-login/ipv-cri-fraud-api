package uk.gov.di.ipv.cri.fraud.api.domain.audit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ExperianCrossCoreResponse {

    @JsonProperty("uCodes")
    private final List<String> uCodes;

    public ExperianCrossCoreResponse(List<String> uCodes) {
        this.uCodes = uCodes;
    }

    public List<String> getuCodes() {
        return uCodes;
    }
}
