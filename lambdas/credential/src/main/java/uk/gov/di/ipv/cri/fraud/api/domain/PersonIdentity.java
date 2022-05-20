package uk.gov.di.ipv.cri.fraud.api.domain;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import java.time.LocalDate;
import java.util.List;

public class PersonIdentity {
    @NotBlank(message = "{personIdentity.firstname.required}")
    private String firstName;

    private String middleNames;

    @NotBlank(message = "{personIdentity.surname.required}")
    private String surname;

    @NotNull(message = "{personIdentity.dateOfBirth.required}")
    @Past(message = "{personIdentity.dateOfBirth.notInFuture}")
    private LocalDate dateOfBirth;

    @Valid
    @NotNull(message = "{personIdentity.addresses.required}")
    @NotEmpty(message = "{personIdentity.addresses.required}")
    private List<PersonAddress> addresses;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleNames() {
        return middleNames;
    }

    public void setMiddleNames(String middleNames) {
        this.middleNames = middleNames;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<PersonAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<PersonAddress> addresses) {
        this.addresses = addresses;
    }
}
