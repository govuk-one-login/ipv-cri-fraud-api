package uk.gov.di.ipv.cri.fraud.api.service;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;

import java.util.List;

class PersonIdentityValidator {
    ValidationResult<List<String>> validate(PersonIdentity personIdentity) {
        return ValidationResult.createValidResult();
    }
}
