package uk.gov.di.ipv.cri.fraud.api.util;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType.CURRENT;

public class TestDataCreator {
    public static PersonIdentity createTestPersonIdentity(AddressType addressType) {
        PersonIdentity personIdentity = new PersonIdentity();
        personIdentity.setDateOfBirth(LocalDate.of(1976, 12, 26));
        Address address = new Address();
        address.setValidFrom(LocalDate.now().minusYears(3));
        if (addressType.equals(AddressType.PREVIOUS)) {
            address.setValidUntil(LocalDate.now().minusMonths(1));
        }

        address.setPostalCode("Postcode");
        address.setStreetName("Street Name");
        address.setAddressLocality("PostTown");
        personIdentity.setAddresses(List.of(address));
        return personIdentity;
    }

    public static PersonIdentity createTestPersonIdentity() {
        return createTestPersonIdentity(CURRENT);
    }
}
