package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocDataOnlyAtCLoc {

    @JsonProperty("startDateOldestPrim")
    private String startDateOldestPrim;

    public String getStartDateOldestPrim() {
        return startDateOldestPrim;
    }

    public void setStartDateOldestPrim(String startDateOldestPrim) {
        this.startDateOldestPrim = startDateOldestPrim;
    }
}
