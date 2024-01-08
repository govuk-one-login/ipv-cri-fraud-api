package uk.gov.di.ipv.cri.fraud.api.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Name;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.fraud.library.config.GlobalConstants;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RequestSentAuditHelperTest {

    @Test
    void ShouldReturnAuditRestrictedFormatFromPersonIdentity() {

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();

        PersonIdentityDetailed testPersonIdentityDetailedFromPersonIdentity =
                RequestSentAuditHelper.personIdentityToAuditRestrictedFormat(personIdentity);

        Name pidName = testPersonIdentityDetailedFromPersonIdentity.getNames().get(0);
        assertEquals(personIdentity.getFirstName(), pidName.getNameParts().get(0).getValue());
        assertEquals(personIdentity.getMiddleNames(), pidName.getNameParts().get(1).getValue());
        assertEquals(personIdentity.getSurname(), pidName.getNameParts().get(2).getValue());
        assertEquals(
                personIdentity.getDateOfBirth(),
                testPersonIdentityDetailedFromPersonIdentity.getBirthDates().get(0).getValue());

        // PersonIdentity With Addresses
        List<Address> piAddresses = personIdentity.getAddresses();
        List<Address> pidAddresses = testPersonIdentityDetailedFromPersonIdentity.getAddresses();

        Address piAddress = piAddresses.get(0);
        Address pidAddress = pidAddresses.get(0);

        assertEquals(piAddress, pidAddress);
        assertEquals(piAddress.getAddressCountry(), GlobalConstants.ADDRESS_COUNTRY);
        assertEquals(pidAddresses.get(0).getAddressCountry(), GlobalConstants.ADDRESS_COUNTRY);
    }
}
