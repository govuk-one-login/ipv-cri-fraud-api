package uk.gov.di.ipv.cri.fraud.api.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Address;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.IdentityVerificationRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.PEPRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Person;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType.CURRENT;
import static uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType.PREVIOUS;

class IdentityVerificationRequestMapperTest {

    private static final int ADDRESSES_TO_GENERATE_IN_TEST = 5;

    private static final String TENANT_ID = "tenant-id";
    private IdentityVerificationRequestMapper requestMapper;
    private PersonIdentity personIdentity;

    @BeforeEach
    void setup() {
        requestMapper = new IdentityVerificationRequestMapper();
    }

    @Test
    void shouldConvertPersonIdentityToCrossCoreApiRequestForCurrentAddress() {
        personIdentity = TestDataCreator.createTestPersonIdentity(CURRENT);
        personIdentity.getAddresses().get(0).setSubBuildingName("Building One");
        personIdentity.getAddresses().get(0).setBuildingName("House Name");
        personIdentity.getAddresses().get(0).setBuildingNumber("44");

        IdentityVerificationRequest result =
                requestMapper.mapPersonIdentity(personIdentity, TENANT_ID);

        assertNotNull(result);

        Person person = result.getPayload().getContacts().get(0).getPerson();

        assertEquals(
                LocalDate.of(1976, 12, 26).toString(), person.getPersonDetails().getDateOfBirth());
        assertEquals(personIdentity.getFirstName(), person.getNames().get(0).getFirstName());
        assertEquals(personIdentity.getSurname(), person.getNames().get(0).getSurName());

        assertEquals("WEB", result.getPayload().getSource());

        Address address = result.getPayload().getContacts().get(0).getAddresses().get(0);

        assertEquals(CURRENT.toString(), address.getAddressType());
        assertEquals("PostTown", address.getPostTown());
        assertEquals("Street Name", address.getStreet());
        assertEquals("Postcode", address.getPostal());
        assertEquals("Building One", address.getSubBuilding());
        assertEquals("House Name", address.getBuildingName());
        assertEquals("44", address.getBuildingNumber());
    }

    @Test
    void shouldConvertPersonIdentityToCrossCoreApiRequestForPreviousAddress() {
        personIdentity = TestDataCreator.createTestPersonIdentity(PREVIOUS);

        IdentityVerificationRequest result =
                requestMapper.mapPersonIdentity(personIdentity, TENANT_ID);

        assertNotNull(result);

        Person person = result.getPayload().getContacts().get(0).getPerson();

        assertEquals(
                LocalDate.of(1976, 12, 26).toString(), person.getPersonDetails().getDateOfBirth());
        assertEquals(personIdentity.getFirstName(), person.getNames().get(0).getFirstName());
        assertEquals(personIdentity.getSurname(), person.getNames().get(0).getSurName());

        assertEquals("WEB", result.getPayload().getSource());

        Address address = result.getPayload().getContacts().get(0).getAddresses().get(0);

        assertEquals(PREVIOUS.toString(), address.getAddressType());
        assertEquals("PostTown", address.getPostTown());
        assertEquals("Street Name", address.getStreet());
        assertEquals("Postcode", address.getPostal());
    }

    @ParameterizedTest
    @MethodSource("getAddressCount")
    void shouldConvertPersonIdentityToCrossCoreApiRequestWithAddressCount(int addressCount) {

        personIdentity =
                TestDataCreator.createTestPersonIdentityMultipleAddresses(
                        addressCount, 0, 0, false);

        IdentityVerificationRequest result =
                requestMapper.mapPersonIdentity(personIdentity, TENANT_ID);

        assertNotNull(result);

        Person person = result.getPayload().getContacts().get(0).getPerson();

        assertEquals(
                LocalDate.of(1976, 12, 26).toString(), person.getPersonDetails().getDateOfBirth());
        assertEquals(personIdentity.getFirstName(), person.getNames().get(0).getFirstName());
        assertEquals(personIdentity.getSurname(), person.getNames().get(0).getSurName());

        assertEquals("WEB", result.getPayload().getSource());

        assertNotEquals(0, addressCount);

        List<Address> addresses = result.getPayload().getContacts().get(0).getAddresses();

        IntStream.range(0, addressCount)
                .forEach(
                        a -> {
                            assertEquals(
                                    a == 0 ? CURRENT.toString() : PREVIOUS.toString(),
                                    addresses.get(a).getAddressType());
                            assertEquals("PostTown" + a, addresses.get(a).getPostTown());
                            assertEquals("Street Name" + a, addresses.get(a).getStreet());
                            assertEquals("Postcode" + a, addresses.get(a).getPostal());
                        });
    }

    @Test
    void shouldThrowExceptionWhenPepPersonIdentityIsNull() {
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> requestMapper.mapPersonIdentity(personIdentity, TENANT_ID));
        assertEquals("The personIdentity must not be null", exception.getMessage());
    }

    @Test
    void shouldConvertPepPersonIdentityToCrossCoreApiRequestForCurrentAddress() {
        personIdentity = TestDataCreator.createTestPersonIdentity(CURRENT);
        personIdentity.getAddresses().get(0).setSubBuildingName("Building One");
        personIdentity.getAddresses().get(0).setBuildingName("House Name");
        personIdentity.getAddresses().get(0).setBuildingNumber("44");

        PEPRequest result = requestMapper.mapPEPPersonIdentity(personIdentity, TENANT_ID);

        assertNotNull(result);

        Person person = result.getPayload().getContacts().get(0).getPerson();

        assertEquals(
                LocalDate.of(1976, 12, 26).toString(), person.getPersonDetails().getDateOfBirth());
        assertEquals(personIdentity.getFirstName(), person.getNames().get(0).getFirstName());
        assertEquals(personIdentity.getSurname(), person.getNames().get(0).getSurName());
        assertEquals("Y", person.getPersonDetails().getPepsSanctionsFlag());
        assertNotNull(person.getPersonDetails().getYearOfBirth());
        assertEquals("APPLICANT", person.getTypeOfPerson());

        assertEquals(
                "MS_CON",
                result.getPayload().getApplication().getProductDetails().getProductCode());

        Address address = result.getPayload().getContacts().get(0).getAddresses().get(0);

        assertEquals(CURRENT.toString(), address.getAddressType());
        assertEquals("PostTown", address.getPostTown());
        assertEquals("Street Name", address.getStreet());
        assertEquals("Postcode", address.getPostal());
        assertEquals("Building One", address.getSubBuilding());
        assertEquals("House Name", address.getBuildingName());
        assertEquals("44", address.getBuildingNumber());
    }

    @ParameterizedTest
    @MethodSource("getAddressCount")
    void shouldConvertPepPersonIdentityToCrossCoreApiRequestFromMultipleAddressToSingleAddress(
            int addressCount) {

        personIdentity =
                TestDataCreator.createTestPersonIdentityMultipleAddresses(
                        addressCount, 0, 0, false);

        PEPRequest result = requestMapper.mapPEPPersonIdentity(personIdentity, TENANT_ID);

        assertNotNull(result);

        Person person = result.getPayload().getContacts().get(0).getPerson();

        assertEquals(
                LocalDate.of(1976, 12, 26).toString(), person.getPersonDetails().getDateOfBirth());
        assertEquals(personIdentity.getFirstName(), person.getNames().get(0).getFirstName());
        assertEquals(personIdentity.getSurname(), person.getNames().get(0).getSurName());
        assertEquals("Y", person.getPersonDetails().getPepsSanctionsFlag());
        assertNotNull(person.getPersonDetails().getYearOfBirth());
        assertEquals("APPLICANT", person.getTypeOfPerson());

        assertEquals(
                "MS_CON",
                result.getPayload().getApplication().getProductDetails().getProductCode());

        assertNotEquals(0, addressCount);

        List<Address> pepAddresses = result.getPayload().getContacts().get(0).getAddresses();

        int numberOfPEPaddresses = pepAddresses.size();

        IntStream.range(0, numberOfPEPaddresses)
                .forEach(
                        a -> {
                            assertEquals(
                                    a == 0 ? CURRENT.toString() : PREVIOUS.toString(),
                                    pepAddresses.get(a).getAddressType());
                            assertEquals("PostTown" + a, pepAddresses.get(a).getPostTown());
                            assertEquals("Street Name" + a, pepAddresses.get(a).getStreet());
                            assertEquals("Postcode" + a, pepAddresses.get(a).getPostal());
                        });

        // Pep Request must only contain 1 address
        assertEquals(1, numberOfPEPaddresses);
    }

    @Test
    void shouldThrowExceptionWhenPersonIdentityIsNull() {
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> requestMapper.mapPersonIdentity(personIdentity, TENANT_ID));
        assertEquals("The personIdentity must not be null", exception.getMessage());
    }

    private static int[] getAddressCount() {
        return IntStream.range(1, ADDRESSES_TO_GENERATE_IN_TEST).toArray();
    }
}
