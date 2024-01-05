package uk.gov.di.ipv.cri.fraud.library;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Name;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.NamePart;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;

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
            nameParts.add(setNamePart(personIdentity.getFirstName(), "GivenName"));
        }

        if (Objects.nonNull(personIdentity.getMiddleNames())) {
            nameParts.add(setNamePart(personIdentity.getMiddleNames(), "GivenName"));
        }

        if (Objects.nonNull(personIdentity.getSurname())) {
            nameParts.add(setNamePart(personIdentity.getSurname(), "FamilyName"));
        }

        Name name1 = new Name();
        name1.setNameParts(nameParts);

        BirthDate birthDate = new BirthDate();
        birthDate.setValue(personIdentity.getDateOfBirth());

        return new PersonIdentityDetailed(
                List.of(name1), List.of(birthDate), personIdentity.getAddresses());
    }

    private static NamePart setNamePart(String value, String type) {
        NamePart namePart = new NamePart();
        namePart.setValue(value);
        namePart.setType(type);
        return namePart;
    }
}
