package uk.gov.di.ipv.cri.fraud.api.util;

import uk.gov.di.ipv.cri.fraud.api.domain.AddressType;
import uk.gov.di.ipv.cri.fraud.api.domain.PersonAddress;
import uk.gov.di.ipv.cri.fraud.api.domain.PersonIdentity;

import java.time.LocalDate;
import java.util.List;

public class TestDataCreator {
    public static PersonIdentity createTestPersonIdentity(AddressType addressType) {
        PersonIdentity personIdentity = new PersonIdentity();
        personIdentity.setDateOfBirth(LocalDate.of(1976, 12, 26));
        PersonAddress address = new PersonAddress();
        address.setAddressType(addressType);
        address.setPostcode("Postcode");
        address.setStreet("Street Name");
        address.setTownCity("PostTown");
        personIdentity.setAddresses(List.of(address));
        return personIdentity;
    }

    public static PersonIdentity createTestPersonIdentity() {
        return createTestPersonIdentity(AddressType.CURRENT);
    }
}
