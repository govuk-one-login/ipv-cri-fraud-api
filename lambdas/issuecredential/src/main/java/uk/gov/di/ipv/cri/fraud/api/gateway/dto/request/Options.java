package uk.gov.di.ipv.cri.fraud.api.gateway.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "customOption1",
    "customOption2",
    "customOption3",
    "customOption4",
    "customOption5",
    "customOption6",
    "customOption7"
})
public class Options {

    @JsonProperty("version")
    private String version;

    @JsonProperty("customOption1")
    private String customOption1;

    @JsonProperty("customOption2")
    private String customOption2;

    @JsonProperty("customOption3")
    private String customOption3;

    @JsonProperty("customOption4")
    private String customOption4;

    @JsonProperty("customOption5")
    private String customOption5;

    @JsonProperty("customOption6")
    private String customOption6;

    @JsonProperty("customOption7")
    private String customOption7;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCustomOption1() {
        return customOption1;
    }

    public void setCustomOption1(String customOption1) {
        this.customOption1 = customOption1;
    }

    public String getCustomOption2() {
        return customOption2;
    }

    public void setCustomOption2(String customOption2) {
        this.customOption2 = customOption2;
    }

    public String getCustomOption3() {
        return customOption3;
    }

    public void setCustomOption3(String customOption3) {
        this.customOption3 = customOption3;
    }

    public String getCustomOption4() {
        return customOption4;
    }

    public void setCustomOption4(String customOption4) {
        this.customOption4 = customOption4;
    }

    public String getCustomOption5() {
        return customOption5;
    }

    public void setCustomOption5(String customOption5) {
        this.customOption5 = customOption5;
    }

    public String getCustomOption6() {
        return customOption6;
    }

    public void setCustomOption6(String customOption6) {
        this.customOption6 = customOption6;
    }

    public String getCustomOption7() {
        return customOption7;
    }

    public void setCustomOption7(String customOption7) {
        this.customOption7 = customOption7;
    }
}
