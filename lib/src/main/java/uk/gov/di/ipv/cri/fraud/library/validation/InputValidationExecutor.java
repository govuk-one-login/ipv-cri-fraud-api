package uk.gov.di.ipv.cri.fraud.library.validation;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InputValidationExecutor {
    private final Validator validator;

    public InputValidationExecutor(Validator validator) {
        this.validator = validator;
    }

    public <T> ValidationResult performInputValidation(T input) {
        Set<ConstraintViolation<T>> violations = this.validator.validate(input);
        List<String> validationErrorMessages =
                violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toList());
        return new ValidationResult<>(violations.isEmpty(), validationErrorMessages);
    }
}
