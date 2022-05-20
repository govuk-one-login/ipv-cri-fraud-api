package uk.gov.di.ipv.cri.fraud.library.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InputValidationExecutorTest {

    private class PersonIdentity {
        @NotBlank(message = "{personIdentity.firstname.required}")
        private String firstName;

        @NotBlank(message = "{personIdentity.surname.required}")
        private String surname;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }
    }

    @Mock private Validator mockValidator;
    private InputValidationExecutor inputValidationExecutor;

    @BeforeEach
    void setUp() {
        inputValidationExecutor = new InputValidationExecutor(mockValidator);
    }

    @Test
    void shouldReturnValidValidationResultWhenValidInputProvided() {
        PersonIdentity personIdentity = new PersonIdentity();
        personIdentity.setFirstName("tommy");
        personIdentity.setSurname("smith");
        when(mockValidator.validate(personIdentity)).thenReturn(new HashSet<>());

        ValidationResult<List<String>> validationResult =
                inputValidationExecutor.performInputValidation(personIdentity);

        assertTrue(validationResult.isValid());
        assertEquals(0, validationResult.getError().size());
    }

    @Test
    void shouldReturnInvalidValidationResultWhenInvalidInputProvided() {
        final String validationErrorMsg = "validation error message";
        final PersonIdentity personIdentity = new PersonIdentity();
        ConstraintViolation<?> mockConstraintViolation = Mockito.mock(ConstraintViolation.class);
        when(mockConstraintViolation.getMessage()).thenReturn(validationErrorMsg);
        Mockito.doReturn(Set.of(mockConstraintViolation))
                .when(mockValidator)
                .validate(personIdentity);

        ValidationResult validationResult =
                inputValidationExecutor.performInputValidation(personIdentity);

        assertFalse(validationResult.isValid());
    }
}
