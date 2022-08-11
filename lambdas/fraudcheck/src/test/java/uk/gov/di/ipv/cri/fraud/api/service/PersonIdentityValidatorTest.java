package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.util.JsonValidationUtility;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonIdentityValidatorTest {

    private static final Logger LOGGER = LogManager.getLogger();

    @Test
    void testPersonIdentityNameCannotBeNull() {

        final String TEST_STRING = null;

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();
        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        personIdentity.setFirstName(TEST_STRING);
        personIdentity.setSurname(TEST_STRING);

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        final String EXPECTED_ERROR_0 =
                "FirstName" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;
        final String EXPECTED_ERROR_1 =
                "Surname" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_STRING, personIdentity.getFirstName());
        assertEquals(TEST_STRING, personIdentity.getSurname());
        assertEquals(2, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR_0, validationResult.getError().get(0));
        assertEquals(EXPECTED_ERROR_1, validationResult.getError().get(1));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testPersonIdentityDOBCannotBeNull() {

        final LocalDate TEST_LOCAL_DATE = null;

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();
        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        personIdentity.setDateOfBirth(TEST_LOCAL_DATE);

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        final String EXPECTED_ERROR =
                "DateOfBirth" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_LOCAL_DATE, personIdentity.getDateOfBirth());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testPersonIdentityAddressesCannotBeNull() {

        final List<Address> TEST_ADDRESSES = null;

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();
        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        personIdentity.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        final String EXPECTED_ERROR =
                "Addresses" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_ADDRESSES, personIdentity.getAddresses());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testPersonIdentityAddressesCannotBeEmpty() {

        final List<Address> TEST_ADDRESSES = new ArrayList<>();

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();
        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        personIdentity.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        final String TEST_INTEGER_NAME = "Addresses";
        final int TEST_VALUE = TEST_ADDRESSES.size();

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_VALUE,
                        PersonIdentityValidator.MIN_SUPPORTED_ADDRESSES,
                        PersonIdentityValidator.MAX_SUPPORTED_ADDRESSES,
                        TEST_INTEGER_NAME);

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_ADDRESSES, personIdentity.getAddresses());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testPersonIdentityTooManyAddressesFail() {

        final int addressChainLength = PersonIdentityValidator.MAX_SUPPORTED_ADDRESSES;
        final int additionalCurrentAddresses = 1;
        final int additionalPreviousAddresses = 0;
        final int TOTAL_ADDRESSES = addressChainLength + additionalCurrentAddresses;
        final boolean shuffleAddresses = true;

        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentityMultipleAddresses(
                        addressChainLength,
                        additionalCurrentAddresses,
                        additionalPreviousAddresses,
                        shuffleAddresses);

        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TOTAL_ADDRESSES,
                        PersonIdentityValidator.MIN_SUPPORTED_ADDRESSES,
                        PersonIdentityValidator.MAX_SUPPORTED_ADDRESSES,
                        "Addresses");

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TOTAL_ADDRESSES, personIdentity.getAddresses().size());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testPersonIdentityAddressesValidCurrentAddressIsOk() {

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(LocalDate.now());

        final List<Address> TEST_ADDRESSES = List.of(TEST_CURRENT_ADDRESS);

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();
        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        personIdentity.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        assertEquals(TEST_ADDRESSES, personIdentity.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testPersonIdentityAddressesCurrentAddressNullDatesIsOk() {
        // Edge case scenario : A current address where user does not know when exactly they moved
        // in (ValidFrom null).

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(null);
        TEST_CURRENT_ADDRESS.setValidUntil(null);

        final List<Address> TEST_ADDRESSES = List.of(TEST_CURRENT_ADDRESS);

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();
        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        personIdentity.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        assertEquals(TEST_ADDRESSES, personIdentity.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testPersonIdentityAddressesCurrentAddressWithFutureDatesIsFail() {

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(LocalDate.now().plusYears(1));
        TEST_CURRENT_ADDRESS.setValidUntil(null);

        final List<Address> TEST_ADDRESSES = List.of(TEST_CURRENT_ADDRESS);

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();
        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        personIdentity.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        final String EXPECTED_ERROR =
                PersonIdentityValidator.createAddressCheckErrorMessage(1, 0, 0, 1);

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_ADDRESSES.size(), personIdentity.getAddresses().size());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testPersonIdentityAddressesValidCurrentAndPreviousAddressIsOk() {

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(LocalDate.of(1999, 12, 31));
        TEST_CURRENT_ADDRESS.setValidUntil(null);

        final Address TEST_PREVIOUS_ADDRESS = new Address();
        TEST_PREVIOUS_ADDRESS.setValidFrom(null);
        TEST_PREVIOUS_ADDRESS.setValidUntil(TEST_CURRENT_ADDRESS.getValidFrom());

        final List<Address> TEST_ADDRESSES = List.of(TEST_CURRENT_ADDRESS, TEST_PREVIOUS_ADDRESS);

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();
        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        personIdentity.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TEST_ADDRESSES, personIdentity.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testPersonIdentityAddressesAValidCurrentAndPreviousAddressAreInReverseOrderIsOk() {

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(LocalDate.now());

        final Address TEST_PREVIOUS_ADDRESS = new Address();
        TEST_PREVIOUS_ADDRESS.setValidUntil(TEST_CURRENT_ADDRESS.getValidFrom().minusDays(1));

        final List<Address> TEST_ADDRESSES = List.of(TEST_PREVIOUS_ADDRESS, TEST_CURRENT_ADDRESS);

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();
        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        personIdentity.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        assertEquals(TEST_ADDRESSES, personIdentity.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testPersonIdentityAddressesValidCurrentAndPreviousAddressOverlapIsOk() {
        // Edge case scenario : A current address where user has moved out of the previous on the
        // current date
        // (CURRENT ValidFrom == PREVIOUS ValidUntil).

        final Address TEST_CURRENT_ADDRESS = new Address();
        TEST_CURRENT_ADDRESS.setValidFrom(LocalDate.now().atTime(0, 0).toLocalDate());

        final Address TEST_PREVIOUS_ADDRESS = new Address();
        TEST_PREVIOUS_ADDRESS.setValidUntil(TEST_CURRENT_ADDRESS.getValidFrom());

        final List<Address> TEST_ADDRESSES = List.of(TEST_CURRENT_ADDRESS, TEST_PREVIOUS_ADDRESS);

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();
        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        personIdentity.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        assertEquals(TEST_ADDRESSES, personIdentity.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testPersonIdentityAddressesMultipleCurrentAddressesIsOk() {

        final Address TEST_CURRENT_ADDRESS_1 = new Address();
        TEST_CURRENT_ADDRESS_1.setValidFrom(LocalDate.now());
        TEST_CURRENT_ADDRESS_1.setValidUntil(null);

        final Address TEST_CURRENT_ADDRESS_2 = new Address();
        TEST_CURRENT_ADDRESS_2.setValidFrom(LocalDate.now());
        TEST_CURRENT_ADDRESS_2.setValidUntil(null);

        final List<Address> TEST_ADDRESSES =
                List.of(TEST_CURRENT_ADDRESS_1, TEST_CURRENT_ADDRESS_2);

        PersonIdentity personIdentity = TestDataCreator.createTestPersonIdentity();
        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        personIdentity.setAddresses(TEST_ADDRESSES);

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        assertEquals(TEST_ADDRESSES, personIdentity.getAddresses());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }

    @Test
    void testPersonIdentityAddressesManyPreviousAddressesFail() {

        final int addressChainLength = 0;
        final int additionalCurrentAddresses = 0;
        final int additionalPreviousAddresses = 2;
        final int TOTAL_ADDRESSES =
                addressChainLength + additionalCurrentAddresses + additionalPreviousAddresses;
        final boolean shuffleAddresses = true;

        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentityMultipleAddresses(
                        addressChainLength,
                        additionalCurrentAddresses,
                        additionalPreviousAddresses,
                        shuffleAddresses);

        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        final String EXPECTED_ERROR =
                PersonIdentityValidator.createAddressCheckErrorMessage(
                        TOTAL_ADDRESSES,
                        additionalCurrentAddresses,
                        additionalPreviousAddresses,
                        0);

        LOGGER.info(validationResult.getError().toString());

        assertEquals(TOTAL_ADDRESSES, personIdentity.getAddresses().size());
        assertEquals(1, validationResult.getError().size());
        assertEquals(EXPECTED_ERROR, validationResult.getError().get(0));
        assertFalse(validationResult.isValid());
    }

    @Test
    void testPersonIdentityAddressesManyValidCurrentAndValidPreviousAddressesOutOfOrderIsOK() {

        final int addressChainLength = 10;
        final int additionalCurrentAddresses = 5;
        final int additionalPreviousAddresses = 5;
        final int TOTAL_ADDRESSES =
                addressChainLength + additionalCurrentAddresses + additionalPreviousAddresses;
        final boolean shuffleAddresses = true;

        PersonIdentity personIdentity =
                TestDataCreator.createTestPersonIdentityMultipleAddresses(
                        addressChainLength,
                        additionalCurrentAddresses,
                        additionalPreviousAddresses,
                        shuffleAddresses);

        PersonIdentityValidator personIdentityValidator = new PersonIdentityValidator();

        ValidationResult<List<String>> validationResult =
                personIdentityValidator.validate(personIdentity);

        assertEquals(TOTAL_ADDRESSES, personIdentity.getAddresses().size());
        assertEquals(0, validationResult.getError().size());
        assertTrue(validationResult.isValid());
    }
}
