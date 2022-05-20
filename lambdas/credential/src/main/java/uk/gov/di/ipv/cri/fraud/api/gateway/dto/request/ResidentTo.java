package uk.gov.di.ipv.cri.fraud.api.gateway.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"fullDateTo", "yearTo", "monthTo", "dayTo"})
public class ResidentTo {

    @JsonProperty("fullDateTo")
    private String fullDateTo;

    @JsonProperty("yearTo")
    private String yearTo;

    @JsonProperty("monthTo")
    private String monthTo;

    @JsonProperty("dayTo")
    private String dayTo;

    public String getFullDateTo() {
        return fullDateTo;
    }

    public void setFullDateTo(String fullDateTo) {
        this.fullDateTo = fullDateTo;
    }

    public String getYearTo() {
        return yearTo;
    }

    public void setYearTo(String yearTo) {
        this.yearTo = yearTo;
    }

    public String getMonthTo() {
        return monthTo;
    }

    public void setMonthTo(String monthTo) {
        this.monthTo = monthTo;
    }

    public String getDayTo() {
        return dayTo;
    }

    public void setDayTo(String dayTo) {
        this.dayTo = dayTo;
    }
}
