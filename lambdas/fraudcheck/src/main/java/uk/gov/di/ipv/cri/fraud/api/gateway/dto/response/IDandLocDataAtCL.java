package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IDandLocDataAtCL {

    @JsonProperty("startDateOldestPrim")
    private String startDateOldestPrim;

    @JsonProperty("startDateOldestSec")
    private String startDateOldestSec;

    public String getStartDateOldestPrim() {
        return startDateOldestPrim;
    }

    public void setStartDateOldestPrim(String startDateOldestPrim) {
        this.startDateOldestPrim = startDateOldestPrim;
    }

    public String getStartDateOldestSec() {
        return startDateOldestSec;
    }

    public void setStartDateOldestSec(String startDateOldestSec) {
        this.startDateOldestSec = startDateOldestSec;
    }
}
