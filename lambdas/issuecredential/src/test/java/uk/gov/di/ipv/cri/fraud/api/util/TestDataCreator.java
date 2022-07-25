package uk.gov.di.ipv.cri.fraud.api.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType.CURRENT;

public class TestDataCreator {

    private static final Logger LOGGER = LogManager.getLogger();

    public static PersonIdentity createTestPersonIdentity(AddressType addressType) {
        PersonIdentity personIdentity = new PersonIdentity();

        personIdentity.setFirstName("FirstName");
        personIdentity.setMiddleNames("MiddleName");
        personIdentity.setSurname("Surname");

        personIdentity.setDateOfBirth(LocalDate.of(1976, 12, 26));
        Address address = new Address();
        address.setValidFrom(LocalDate.now().minusYears(3));
        if (addressType.equals(AddressType.PREVIOUS)) {
            address.setValidUntil(LocalDate.now().minusMonths(1));
        }

        address.setBuildingNumber("101");
        address.setStreetName("Street Name");
        address.setAddressLocality("PostTown");
        address.setPostalCode("Postcode");
        address.setAddressCountry("GB");

        personIdentity.setAddresses(List.of(address));
        return personIdentity;
    }

    public static PersonIdentity createTestPersonIdentity() {
        return createTestPersonIdentity(CURRENT);
    }

    public static PersonIdentity createTestPersonIdentityMultipleAddresses(int totalAddresses) {
        PersonIdentity personIdentity = new PersonIdentity();

        personIdentity.setFirstName("FirstName");
        personIdentity.setMiddleNames("MiddleName");
        personIdentity.setSurname("Surname");

        personIdentity.setDateOfBirth(LocalDate.of(1976, 12, 26));

        List<Address> addresses = new ArrayList<>();
        IntStream.range(0, totalAddresses)
                .forEach(
                        a -> {
                            addresses.add(createAddress(a));
                        });

        personIdentity.setAddresses(addresses);

        return personIdentity;
    }

    private static Address createAddress(int id) {

        Address address = new Address();

        final int yearsBetweenAddresses = 2;

        int startYear = id + (id + yearsBetweenAddresses);
        int endYear = id + id;

        address.setValidFrom(LocalDate.now().minusYears(startYear));

        address.setValidUntil(
                (id == 0 ? null : LocalDate.now().minusYears(endYear).minusMonths(1)));

        address.setBuildingNumber(String.valueOf((id + 100)));
        address.setPostalCode("Postcode" + id);
        address.setStreetName("Street Name" + id);
        address.setAddressLocality("PostTown" + id);

        LOGGER.info(
                "createAddress "
                        + id
                        + " "
                        + address.getAddressType()
                        + " from "
                        + address.getValidFrom()
                        + " until "
                        + address.getValidUntil());

        return address;
    }
}
