package uk.gov.di.ipv.cri.fraud.api.gateway.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Application {
    @JsonProperty("applicants")
    private List<Applicant> applicants = new ArrayList<>();

    @JsonProperty("productDetails")
    private ProductDetails productDetails;

    @JsonProperty("status")
    private String status;

    @JsonProperty("originalRequestTime")
    private String originalRequestTime;

    @JsonProperty("type")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOriginalRequestTime() {
        return originalRequestTime;
    }

    public void setOriginalRequestTime(String originalRequestTime) {
        this.originalRequestTime = originalRequestTime;
    }

    public List<Applicant> getApplicants() {
        return applicants;
    }

    public void setApplicants(List<Applicant> applicants) {
        this.applicants = applicants;
    }

    public ProductDetails getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(ProductDetails productDetails) {
        this.productDetails = productDetails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
