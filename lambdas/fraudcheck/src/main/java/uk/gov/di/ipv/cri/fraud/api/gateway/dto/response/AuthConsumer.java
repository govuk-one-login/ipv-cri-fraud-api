package uk.gov.di.ipv.cri.fraud.api.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthConsumer {

    @JsonProperty("locDataOnlyAtCLoc")
    private LocDataOnlyAtCLoc locDataOnlyAtCLoc;

    @JsonProperty("idandLocDataAtCL")
    private IDandLocDataAtCL idandLocDataAtCL;

    public LocDataOnlyAtCLoc getLocDataOnlyAtCLoc() {
        return locDataOnlyAtCLoc;
    }

    public void setLocDataOnlyAtCLoc(LocDataOnlyAtCLoc locDataOnlyAtCLoc) {
        this.locDataOnlyAtCLoc = locDataOnlyAtCLoc;
    }

    public IDandLocDataAtCL getIdandLocDataAtCL() {
        return idandLocDataAtCL;
    }

    public void setIdandLocDataAtCL(IDandLocDataAtCL idandLocDataAtCL) {
        this.idandLocDataAtCL = idandLocDataAtCL;
    }
}
