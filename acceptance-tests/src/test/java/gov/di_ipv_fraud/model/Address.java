package gov.di_ipv_fraud.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {
    private Long uprn;
    private String organisationName;
    private String departmentName;
    private String subBuildingName;
    private String buildingNumber;
    private String buildingName;
    private String dependentStreetName;
    private String streetName;
    private String doubleDependentAddressLocality;
    private String dependentAddressLocality;
    private String addressLocality;
    private String postalCode;
    private String addressCountry;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate validFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    public Long getUprn() {
        return this.uprn;
    }

    public void setUprn(Long uprn) {
        this.uprn = uprn;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getSubBuildingName() {
        return subBuildingName;
    }

    public void setSubBuildingName(String subBuildingName) {
        this.subBuildingName = subBuildingName;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public String getDependentStreetName() {
        return dependentStreetName;
    }

    public void setDependentStreetName(String dependentStreetName) {
        this.dependentStreetName = dependentStreetName;
    }

    public String getDoubleDependentAddressLocality() {
        return doubleDependentAddressLocality;
    }

    public void setDoubleDependentAddressLocality(String doubleDependentAddressLocality) {
        this.doubleDependentAddressLocality = doubleDependentAddressLocality;
    }

    public String getDependentAddressLocality() {
        return dependentAddressLocality;
    }

    public void setDependentAddressLocality(String dependentAddressLocality) {
        this.dependentAddressLocality = dependentAddressLocality;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getAddressLocality() {
        return addressLocality;
    }

    public void setAddressLocality(String addressLocality) {
        this.addressLocality = addressLocality;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    @JsonIgnore
    public AddressType getAddressType() {

        boolean validFromIsSpecified = Objects.nonNull(this.getValidFrom());
        boolean validUntilIsSpecified = Objects.nonNull(this.getValidUntil());

        if (!validUntilIsSpecified
                && (!validFromIsSpecified || isPastDateOrToday(this.getValidFrom()))) {
            return AddressType.CURRENT;
        }

        if ((validUntilIsSpecified && isPastDateOrToday(this.getValidUntil()))
                && (!validFromIsSpecified
                        || isPastDate(this.getValidFrom())
                                && this.getValidUntil().isAfter(this.getValidFrom()))) {
            return AddressType.PREVIOUS;
        }

        return null;
    }

    private ChronoLocalDate getDateToday() {
        return ChronoLocalDate.from(ZonedDateTime.now());
    }

    private boolean isPastDate(LocalDate input) {
        return input.compareTo(getDateToday()) < 0;
    }

    private boolean isPastDateOrToday(LocalDate input) {
        return input.compareTo(getDateToday()) <= 0;
    }
}
