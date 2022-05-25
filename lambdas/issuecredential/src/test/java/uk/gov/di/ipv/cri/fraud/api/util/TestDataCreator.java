package uk.gov.di.ipv.cri.fraud.api.util;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonAddress;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonAddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;

import java.time.LocalDate;
import java.util.List;

public class TestDataCreator {
    public static PersonIdentity createTestPersonIdentity(PersonAddressType addressType) {
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
        return createTestPersonIdentity(PersonAddressType.CURRENT);
    }
}
