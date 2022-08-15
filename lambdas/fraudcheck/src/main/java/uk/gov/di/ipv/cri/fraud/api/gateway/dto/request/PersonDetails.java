package uk.gov.di.ipv.cri.fraud.api.gateway.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonDetails {

    @JsonProperty("dateOfBirth")
    private String dateOfBirth;

    @JsonProperty("pepsSanctionsFlag")
    private String pepsSanctionsFlag;

    @JsonProperty("yearOfBirth")
    private String yearOfBirth;

    public String getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(String yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public String getPepsSanctionsFlag() {
        return pepsSanctionsFlag;
    }

    public void setPepsSanctionsFlag(String pepsSanctionsFlag) {
        this.pepsSanctionsFlag = pepsSanctionsFlag;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
