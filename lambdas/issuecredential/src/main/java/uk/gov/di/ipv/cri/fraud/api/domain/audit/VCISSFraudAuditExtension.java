package uk.gov.di.ipv.cri.fraud.api.domain.audit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.di.ipv.cri.fraud.api.domain.Evidence;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"iss", "evidence"})
public class VCISSFraudAuditExtension {

    @JsonProperty("iss")
    private final String iss;

    @JsonProperty("evidence")
    private final List<Evidence> evidence;

    @JsonCreator
    public VCISSFraudAuditExtension(
            @JsonProperty(value = "iss", required = true) String iss,
            @JsonProperty(value = "evidence", required = true) List<Evidence> evidence) {
        this.iss = iss;
        this.evidence = evidence;
    }

    public String getIss() {
        return iss;
    }

    public List<Evidence> getEvidence() {
        return evidence;
    }
}
