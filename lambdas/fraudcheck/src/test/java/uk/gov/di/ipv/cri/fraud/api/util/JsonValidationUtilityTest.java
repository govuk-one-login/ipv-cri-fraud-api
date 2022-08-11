package uk.gov.di.ipv.cri.fraud.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonValidationUtilityTest {

    private static List<String> TEST_LIST;
    private static String TEST_STRING;
    private static int TEST_INT;
    private static final int TEST_INT_RANGE_MIN = 0;
    private static final int TEST_INT_RANGE_MAX = 127;
    private static List<String> validationErrors;

    private static final String TEST_LIST_NAME = "TestList";
    private static final String TEST_STRING_NAME = "TestString";
    private static final String TEST_INTEGER_NAME = "TestInteger";

    @BeforeEach
    public void PreEachTestSetup() {
        TEST_LIST = new ArrayList<>();
        TEST_STRING = "";
        TEST_INT = 0;
        validationErrors = new ArrayList<>();
    }

    @Test
    void staticJsonValidationUtilityClassIsFinal() {
        boolean finalClass = Modifier.isFinal(JsonValidationUtility.class.getModifiers());
        assertTrue(finalClass);
    }

    @Test
    void staticJsonValidationUtilityClassCannotBeInstantiated() throws NoSuchMethodException {

        Constructor constructor = JsonValidationUtility.class.getDeclaredConstructor();

        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> constructor.newInstance());

        boolean privateConstructor = Modifier.isPrivate(constructor.getModifiers());

        assertTrue(privateConstructor);
    }

    @Test
    void validateListDataEmptyIsAllowed_PassesWithEmptyList() {
        boolean result =
                JsonValidationUtility.validateListDataEmptyIsAllowed(
                        TEST_LIST, TEST_LIST_NAME, validationErrors);

        assertEquals(0, TEST_LIST.size());
        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateListDataEmptyIsAllowed_PassesWithFilledList() {
        TEST_STRING = "Test";
        TEST_LIST.add(TEST_STRING);

        boolean result =
                JsonValidationUtility.validateListDataEmptyIsAllowed(
                        TEST_LIST, TEST_LIST_NAME, validationErrors);

        assertEquals(1, TEST_LIST.size());
        assertEquals(TEST_STRING, TEST_LIST.get(0));
        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateListDataEmptyIsAllowed_FailsWithNullList() {
        TEST_LIST = null;

        final String EXPECTED_ERROR =
                TEST_LIST_NAME + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        boolean result =
                JsonValidationUtility.validateListDataEmptyIsAllowed(
                        TEST_LIST, TEST_LIST_NAME, validationErrors);

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateListDataEmptyIsFail_PassesWithFilledList() {
        TEST_STRING = "Test";
        TEST_LIST.add(TEST_STRING);

        boolean result =
                JsonValidationUtility.validateListDataEmptyIsFail(
                        TEST_LIST, TEST_LIST_NAME, validationErrors);

        assertEquals(1, TEST_LIST.size());
        assertEquals(TEST_STRING, TEST_LIST.get(0));
        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateListDataEmptyIsFail_FailsWithNullList() {
        TEST_LIST = null;

        final String EXPECTED_ERROR =
                TEST_LIST_NAME + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        boolean result =
                JsonValidationUtility.validateListDataEmptyIsFail(
                        TEST_LIST, TEST_LIST_NAME, validationErrors);

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateListDataEmptyIsFail_FailsWithEmptyList() {
        TEST_LIST = new ArrayList<>();

        final String EXPECTED_ERROR =
                TEST_LIST_NAME + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        boolean result =
                JsonValidationUtility.validateListDataEmptyIsFail(
                        TEST_LIST, TEST_LIST_NAME, validationErrors);

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateStringDataEmptyIsFail_PassesWithFilledString() {
        TEST_STRING = "Test";
        final int TEST_LENGTH = TEST_STRING.length();

        boolean result =
                JsonValidationUtility.validateStringDataEmptyIsFail(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateStringDataEmptyIsFail_FailsWithEmptyString() {
        TEST_STRING = "";
        final int TEST_LENGTH = Integer.MAX_VALUE;

        final String EXPECTED_ERROR =
                TEST_STRING_NAME + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        boolean result =
                JsonValidationUtility.validateStringDataEmptyIsFail(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateStringDataEmptyIsFail_FailsWithNullString() {
        TEST_STRING = null;
        final int TEST_LENGTH = Integer.MAX_VALUE;

        final String EXPECTED_ERROR =
                TEST_STRING_NAME + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        boolean result =
                JsonValidationUtility.validateStringDataEmptyIsFail(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateStringDataEmptyIsFail_FailsWithTooLongString() {

        TEST_STRING = "Test";
        final int TEST_LENGTH = TEST_STRING.length() - 1;

        final String EXPECTED_ERROR =
                TEST_STRING_NAME + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        boolean result =
                JsonValidationUtility.validateStringDataEmptyIsFail(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateStringDataEmptyIsAllowed_PassesWithFilledString() {
        TEST_STRING = "Test";
        final int TEST_LENGTH = TEST_STRING.length();

        boolean result =
                JsonValidationUtility.validateStringDataEmptyIsAllowed(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateStringDataEmptyIsAllowed_PassesWithEmptyString() {
        TEST_STRING = "";
        final int TEST_LENGTH = TEST_STRING.length();

        boolean result =
                JsonValidationUtility.validateStringDataEmptyIsAllowed(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateStringDataEmptyIsAllowed_FailsWithNullString() {
        TEST_STRING = null;
        final int TEST_LENGTH = Integer.MAX_VALUE;

        final String EXPECTED_ERROR =
                TEST_STRING_NAME + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        boolean result =
                JsonValidationUtility.validateStringDataEmptyIsAllowed(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateStringDataEmptyIsAllowed_FailsWithTooLongString() {
        TEST_STRING = "Test";
        final int TEST_LENGTH = TEST_STRING.length() - 1;

        final String EXPECTED_ERROR =
                TEST_STRING_NAME + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        boolean result =
                JsonValidationUtility.validateStringDataEmptyIsAllowed(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateStringDataNullAndEmptyIsAllowedPassesWithFilledString() {
        TEST_STRING = "Test";
        final int TEST_LENGTH = TEST_STRING.length();

        boolean result =
                JsonValidationUtility.validateStringDataNullAndEmptyIsAllowed(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateStringDataNullAndEmptyIsAllowed_PassesWithEmptyString() {
        TEST_STRING = "";
        final int TEST_LENGTH = TEST_STRING.length();

        boolean result =
                JsonValidationUtility.validateStringDataNullAndEmptyIsAllowed(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateStringDataNullAndEmptyIsAllowed_PassesWithNullString() {
        TEST_STRING = null;
        final int TEST_LENGTH = Integer.MAX_VALUE;

        boolean result =
                JsonValidationUtility.validateStringDataNullAndEmptyIsAllowed(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateStringDataNullAndEmptyIsAllowed_FailsWithTooLongString() {
        TEST_STRING = "Test";
        final int TEST_LENGTH = TEST_STRING.length() - 1;

        final String EXPECTED_ERROR =
                TEST_STRING_NAME + JsonValidationUtility.IS_TOO_LONG_ERROR_MESSAGE_SUFFIX;

        boolean result =
                JsonValidationUtility.validateStringDataNullAndEmptyIsAllowed(
                        TEST_STRING, TEST_LENGTH, TEST_STRING_NAME, validationErrors);

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateTimeStampData_PassesWithValidTimeStamp() {
        TEST_STRING = "2022-01-01T00:00:01Z";

        boolean result =
                JsonValidationUtility.validateTimeStampData(
                        TEST_STRING, TEST_STRING_NAME, validationErrors);

        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateTimeStampData_FailsWithNullString() {
        TEST_STRING = null;

        boolean result =
                JsonValidationUtility.validateTimeStampData(
                        TEST_STRING, TEST_STRING_NAME, validationErrors);

        final String EXPECTED_ERROR =
                TEST_STRING_NAME + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX;

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateTimeStampData_FailsWithEmptyString() {
        TEST_STRING = "";

        boolean result =
                JsonValidationUtility.validateTimeStampData(
                        TEST_STRING, TEST_STRING_NAME, validationErrors);

        final String EXPECTED_ERROR =
                TEST_STRING_NAME + JsonValidationUtility.IS_EMPTY_ERROR_MESSAGE_SUFFIX;

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateTimeStampData_FailsWithInvalidTimeStamp() {
        TEST_STRING = "123";

        boolean result =
                JsonValidationUtility.validateTimeStampData(
                        TEST_STRING, TEST_STRING_NAME, validationErrors);

        final String EXPECTED_ERROR =
                TEST_STRING_NAME
                        + JsonValidationUtility.FAIL_PARSING_TIMESTAMP_ERROR_MESSAGE_SUFFIX;

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateIntegerRangeData_PassesWithinRange() {
        TEST_INT = 63;

        boolean result =
                JsonValidationUtility.validateIntegerRangeData(
                        TEST_INT,
                        TEST_INT_RANGE_MIN,
                        TEST_INT_RANGE_MAX,
                        "TEST_INTEGER_NAME",
                        validationErrors);

        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateIntegerRangeData_PassesAtMinRange() {
        TEST_INT = TEST_INT_RANGE_MIN;

        boolean result =
                JsonValidationUtility.validateIntegerRangeData(
                        TEST_INT,
                        TEST_INT_RANGE_MIN,
                        TEST_INT_RANGE_MAX,
                        "TEST_INTEGER_NAME",
                        validationErrors);

        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateIntegerRangeData_PassesAtMaxRange() {
        TEST_INT = TEST_INT_RANGE_MAX;

        boolean result =
                JsonValidationUtility.validateIntegerRangeData(
                        TEST_INT,
                        TEST_INT_RANGE_MIN,
                        TEST_INT_RANGE_MAX,
                        "TEST_INTEGER_NAME",
                        validationErrors);

        assertEquals(0, validationErrors.size());
        assertTrue(result);
    }

    @Test
    void validateIntegerRangeData_FailsUnderMinRange() {
        TEST_INT = TEST_INT_RANGE_MIN - 1;

        boolean result =
                JsonValidationUtility.validateIntegerRangeData(
                        TEST_INT,
                        TEST_INT_RANGE_MIN,
                        TEST_INT_RANGE_MAX,
                        TEST_INTEGER_NAME,
                        validationErrors);

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_INT, TEST_INT_RANGE_MIN, TEST_INT_RANGE_MAX, TEST_INTEGER_NAME);

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }

    @Test
    void validateIntegerRangeData_FailsOverMaxRange() {
        TEST_INT = TEST_INT_RANGE_MAX + 1;

        boolean result =
                JsonValidationUtility.validateIntegerRangeData(
                        TEST_INT,
                        TEST_INT_RANGE_MIN,
                        TEST_INT_RANGE_MAX,
                        TEST_INTEGER_NAME,
                        validationErrors);

        final String EXPECTED_ERROR =
                JsonValidationUtility.createIntegerRangeErrorMessage(
                        TEST_INT, TEST_INT_RANGE_MIN, TEST_INT_RANGE_MAX, TEST_INTEGER_NAME);

        assertEquals(1, validationErrors.size());
        assertEquals(EXPECTED_ERROR, validationErrors.get(0));
        assertFalse(result);
    }
}
