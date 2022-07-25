package uk.gov.di.ipv.cri.fraud.api.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class PersonIdentityValidator {
    ValidationResult<List<String>> validate(PersonIdentity personIdentity) {
        List<String> validationErrors = new ArrayList<>();
        if (StringUtils.isBlank(personIdentity.getFirstName())) {
            validationErrors.add("firstname must not be null or empty");
        }
        if (StringUtils.isBlank(personIdentity.getSurname())) {
            validationErrors.add("surname must not be null or empty");
        }
        if (Objects.isNull(personIdentity.getDateOfBirth())) {
            validationErrors.add("date of birth must not be null");
        }
        if (Objects.isNull(personIdentity.getAddresses())) {
            validationErrors.add("Addresses must not be null");
        } else if (personIdentity.getAddresses().isEmpty()) {
            validationErrors.add("Addresses must not be empty");
        }

        // this implementation needs completing to validate all necessary fields
        return new ValidationResult<>(validationErrors.isEmpty(), validationErrors);
    }
}
