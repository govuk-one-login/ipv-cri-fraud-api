package uk.gov.di.ipv.cri.fraud.api.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.LocalDate;

public class PersonAddress {
    private String houseNameNumber;

    @NotBlank(message = "{personAddress.street.required}")
    private String street;

    @NotBlank(message = "{personAddress.townCity.required}")
    private String townCity;

    @NotBlank(message = "{personAddress.postcode.required}")
    private String postcode;

    @NotNull(message = "{personAddress.addressType.required}")
    private AddressType addressType;

    private LocalDate dateMovedOut;

    public String getHouseNameNumber() {
        return houseNameNumber;
    }

    public void setHouseNameNumber(String houseNameNumber) {
        this.houseNameNumber = houseNameNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getTownCity() {
        return townCity;
    }

    public void setTownCity(String townCity) {
        this.townCity = townCity;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public LocalDate getDateMovedOut() {
        return dateMovedOut;
    }

    public void setDateMovedOut(LocalDate dateMovedOut) {
        this.dateMovedOut = dateMovedOut;
    }
}
