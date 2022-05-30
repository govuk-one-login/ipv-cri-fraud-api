package uk.gov.di.ipv.cri.fraud.api.gateway.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"fullDateFrom", "yearFrom", "monthFrom", "dayFrom"})
public class ResidentFrom {

    @JsonProperty("fullDateFrom")
    private String fullDateFrom;

    @JsonProperty("yearFrom")
    private String yearFrom;

    @JsonProperty("monthFrom")
    private String monthFrom;

    @JsonProperty("dayFrom")
    private String dayFrom;

    public String getFullDateFrom() {
        return fullDateFrom;
    }

    public void setFullDateFrom(String fullDateFrom) {
        this.fullDateFrom = fullDateFrom;
    }

    public String getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(String yearFrom) {
        this.yearFrom = yearFrom;
    }

    public String getMonthFrom() {
        return monthFrom;
    }

    public void setMonthFrom(String monthFrom) {
        this.monthFrom = monthFrom;
    }

    public String getDayFrom() {
        return dayFrom;
    }

    public void setDayFrom(String dayFrom) {
        this.dayFrom = dayFrom;
    }
}
