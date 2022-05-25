package uk.gov.di.ipv.cri.fraud.api.gateway.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "addressIdentifier",
    "addressType",
    "subBuilding",
    "buildingName",
    "buildingNumber",
    "street",
    "locality",
    "postTown",
    "postal",
    "residentFrom",
    "residentTo",
    "timeAtAddress"
})
public class Address {

    @JsonProperty("id")
    private String id;

    @JsonProperty("addressIdentifier")
    private String addressIdentifier;

    @JsonProperty("addressType")
    private String addressType;

    @JsonProperty("subBuilding")
    private String subBuilding;

    @JsonProperty("buildingName")
    private String buildingName;

    @JsonProperty("buildingNumber")
    private String buildingNumber;

    @JsonProperty("street")
    private String street;

    @JsonProperty("locality")
    private String locality;

    @JsonProperty("postTown")
    private String postTown;

    @JsonProperty("postal")
    private String postal;

    @JsonProperty("residentFrom")
    private ResidentFrom residentFrom;

    @JsonProperty("residentTo")
    private ResidentTo residentTo;

    @JsonProperty("timeAtAddress")
    private TimeAtAddress timeAtAddress;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddressIdentifier() {
        return addressIdentifier;
    }

    public void setAddressIdentifier(String addressIdentifier) {
        this.addressIdentifier = addressIdentifier;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getSubBuilding() {
        return subBuilding;
    }

    public void setSubBuilding(String subBuilding) {
        this.subBuilding = subBuilding;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPostTown() {
        return postTown;
    }

    public void setPostTown(String postTown) {
        this.postTown = postTown;
    }

    public String getPostal() {
        return postal;
    }

    public void setPostal(String postal) {
        this.postal = postal;
    }

    public ResidentFrom getResidentFrom() {
        return residentFrom;
    }

    public void setResidentFrom(ResidentFrom residentFrom) {
        this.residentFrom = residentFrom;
    }

    public ResidentTo getResidentTo() {
        return residentTo;
    }

    public void setResidentTo(ResidentTo residentTo) {
        this.residentTo = residentTo;
    }

    public TimeAtAddress getTimeAtAddress() {
        return timeAtAddress;
    }

    public void setTimeAtAddress(TimeAtAddress timeAtAddress) {
        this.timeAtAddress = timeAtAddress;
    }
}
