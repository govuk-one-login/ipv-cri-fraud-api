package uk.gov.di.ipv.cri.fraud.api.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.IdentityVerificationRequest;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType.CURRENT;
import static uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType.PREVIOUS;

class IdentityVerificationRequestMapperTest {

    private static final String TENANT_ID = "tenant-id";
    private IdentityVerificationRequestMapper requestMapper;
    private PersonIdentity personIdentity;

    @BeforeEach
    void setup() {
        requestMapper = new IdentityVerificationRequestMapper(TENANT_ID);
    }

    @Test
    void shouldConvertPersonIdentityToCrossCoreApiRequestForCurrentAddress() {
        personIdentity = TestDataCreator.createTestPersonIdentity(CURRENT);

        IdentityVerificationRequest result = requestMapper.mapPersonIdentity(personIdentity);

        assertNotNull(result);
        assertEquals(
                LocalDate.of(1976, 12, 26).toString(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getPersonDetails()
                        .getDateOfBirth());
        assertEquals(
                personIdentity.getFirstName(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getNames()
                        .get(0)
                        .getFirstName());
        assertEquals(
                personIdentity.getSurname(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getNames()
                        .get(0)
                        .getSurName());
        assertEquals(
                CURRENT.toString(),
                result.getPayload().getContacts().get(0).getAddresses().get(0).getAddressType());
        assertEquals(
                "PostTown",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getPostTown());
        assertEquals(
                "Street Name",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getStreet());
        assertEquals(
                "Postcode",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getPostal());
    }

    @Test
    void shouldConvertPersonIdentityToCrossCoreApiRequestForPreviousAddress() {
        personIdentity = TestDataCreator.createTestPersonIdentity(PREVIOUS);

        IdentityVerificationRequest result = requestMapper.mapPersonIdentity(personIdentity);

        assertNotNull(result);
        assertEquals(
                LocalDate.of(1976, 12, 26).toString(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getPersonDetails()
                        .getDateOfBirth());
        assertEquals(
                personIdentity.getFirstName(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getNames()
                        .get(0)
                        .getFirstName());
        assertEquals(
                personIdentity.getSurname(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getNames()
                        .get(0)
                        .getSurName());
        assertEquals(
                PREVIOUS.toString(),
                result.getPayload().getContacts().get(0).getAddresses().get(0).getAddressType());
        assertEquals(
                "PostTown",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getPostTown());
        assertEquals(
                "Street Name",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getStreet());
        assertEquals(
                "Postcode",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getPostal());
    }

    @Test
    void shouldThrowExceptionWhenPersonIdentityIsNull() {
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> requestMapper.mapPersonIdentity(personIdentity));
        assertEquals("The personIdentity must not be null", exception.getMessage());
    }
}
