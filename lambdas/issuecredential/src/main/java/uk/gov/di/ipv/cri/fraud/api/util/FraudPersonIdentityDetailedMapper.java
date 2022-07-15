package uk.gov.di.ipv.cri.fraud.api.util;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FraudPersonIdentityDetailedMapper {

    private FraudPersonIdentityDetailedMapper() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static PersonIdentityDetailed generatePersonIdentityDetailed(
            PersonIdentity personIdentity) {

        List<NamePart> nameParts = new ArrayList<>();

        if (Objects.nonNull(personIdentity.getFirstName())) {
            NamePart givenName1 = new NamePart();
            givenName1.setValue(personIdentity.getFirstName());
            givenName1.setType("GivenName");
            nameParts.add(givenName1);
        }

        if (Objects.nonNull(personIdentity.getMiddleNames())) {
            NamePart givenName2 = new NamePart();
            givenName2.setValue(personIdentity.getMiddleNames());
            givenName2.setType("GivenName");
            nameParts.add(givenName2);
        }

        if (Objects.nonNull(personIdentity.getSurname())) {
            NamePart familyName = new NamePart();
            familyName.setValue(personIdentity.getSurname());
            familyName.setType("FamilyName");
            nameParts.add(familyName);
        }

        Name name1 = new Name();
        name1.setNameParts(nameParts);

        BirthDate birthDate = new BirthDate();
        birthDate.setValue(personIdentity.getDateOfBirth());

        return new PersonIdentityDetailed(
                List.of(name1), List.of(birthDate), personIdentity.getAddresses());
    }
}
