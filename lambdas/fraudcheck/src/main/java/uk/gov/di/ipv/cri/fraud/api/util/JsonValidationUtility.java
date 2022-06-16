package uk.gov.di.ipv.cri.fraud.api.util;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Utility class used to validate JSON. */
public final class JsonValidationUtility {

    public static final String IS_NULL_ERROR_MESSAGE_SUFFIX = " not found.";
    public static final String IS_EMPTY_ERROR_MESSAGE_SUFFIX = " is empty.";
    public static final String IS_TOO_LONG_ERROR_MESSAGE_SUFFIX = " is too long.";
    public static final String INVALID_VALUE_RANGE_ERROR_MESSAGE_SUFFIX =
            " is outside the valid range.";
    public static final String FAIL_PARSING_TIMESTAMP_ERROR_MESSAGE_SUFFIX =
            " failed timestamp parsing.";

    private JsonValidationUtility() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    /**
     * Test that a list is not null.
     *
     * @param variable The variable being validated
     * @param name String name of the field being validated
     * @param validationErrors A list in which to collect validation errors
     * @return true if validation passed or false if not.
     */
    public static boolean validateListDataEmptyIsAllowed(
            List<?> variable, String name, final List<String> validationErrors) {
        if (variable == null) {
            validationErrors.add(name + IS_NULL_ERROR_MESSAGE_SUFFIX);
            return false;
        }
        return true;
    }

    /**
     * Test that a list is not null or empty.
     *
     * @param variable The variable being validated
     * @param name String name of the field being validated
     * @param validationErrors A list in which to collect validation errors
     * @return true if validation passed or false if not.
     */
    public static boolean validateListDataEmptyIsFail(
            List<?> variable, String name, final List<String> validationErrors) {
        if (variable != null) {
            if (variable.isEmpty()) {
                validationErrors.add(name + IS_EMPTY_ERROR_MESSAGE_SUFFIX);
                return false;
            }
        } else {
            validationErrors.add(name + IS_NULL_ERROR_MESSAGE_SUFFIX);
            return false;
        }

        return true;
    }

    /**
     * Test that a String is not null, Empty or too large.
     *
     * @param variable The variable being validated
     * @param name String name of the field being validated
     * @param validationErrors A list in which to collect validation errors
     * @return true if validation passed or false if not.
     */
    public static boolean validateStringDataEmptyIsFail(
            String variable, int maxLength, String name, final List<String> validationErrors) {
        if (variable != null) {
            if (!variable.isBlank()) {
                if (variable.length() > maxLength) {
                    validationErrors.add(name + IS_TOO_LONG_ERROR_MESSAGE_SUFFIX);
                    return false;
                }
            } else {
                validationErrors.add(name + IS_EMPTY_ERROR_MESSAGE_SUFFIX);
                return false;
            }
        } else {
            validationErrors.add(name + IS_NULL_ERROR_MESSAGE_SUFFIX);
            return false;
        }

        return true;
    }

    /**
     * Test that a String is not null or too large.
     *
     * @param variable The variable being validated
     * @param name String name of the field being validated
     * @param validationErrors A list in which to collect validation errors
     * @return true if validation passed or false if not.
     */
    public static boolean validateStringDataEmptyIsAllowed(
            String variable, int maxLength, String name, final List<String> validationErrors) {
        if (variable != null) {
            if (variable.length() > maxLength) {
                validationErrors.add(name + IS_TOO_LONG_ERROR_MESSAGE_SUFFIX);
                return false;
            }
        } else {
            validationErrors.add(name + IS_NULL_ERROR_MESSAGE_SUFFIX);
            return false;
        }

        return true;
    }

    /**
     * Test a String which can be null.
     *
     * @param variable The variable being validated
     * @param name String name of the field being validated
     * @param validationErrors A list in which to collect validation errors
     * @return true if validation passed or false if not.
     */
    public static boolean validateStringDataNullAndEmptyIsAllowed(
            String variable, int maxLength, String name, final List<String> validationErrors) {
        if (variable != null && (variable.length() > maxLength)) {
            validationErrors.add(name + IS_TOO_LONG_ERROR_MESSAGE_SUFFIX);
            return false;
        }

        return true;
    }

    /**
     * Test that a Timestamp held in a string can be parsed.
     *
     * @param variable The variable being validated
     * @param name String name of the field being validated
     * @param validationErrors A list in which to collect validation errors
     * @return true if validation passed or false if not.
     */
    public static boolean validateTimeStampData(
            String variable, String name, final List<String> validationErrors) {
        if (variable != null) {
            if (!variable.isBlank()) {
                try {
                    Instant.parse(variable);
                } catch (DateTimeParseException e) {
                    validationErrors.add(name + FAIL_PARSING_TIMESTAMP_ERROR_MESSAGE_SUFFIX);
                    return false;
                }
            } else {
                validationErrors.add(name + IS_EMPTY_ERROR_MESSAGE_SUFFIX);
                return false;
            }
        } else {
            validationErrors.add(name + IS_NULL_ERROR_MESSAGE_SUFFIX);
            return false;
        }

        return true;
    }

    /**
     * Test that a value held in an integer is within the range min - max (inclusive).
     *
     * @param variable The variable being validated
     * @param name String name of the field being validated
     * @param validationErrors A list in which to collect validation errors
     * @return true if validation passed or false if not.
     */
    public static boolean validateIntegerRangeData(
            int variable, int min, int max, String name, final List<String> validationErrors) {
        if (variable < min || variable > max) {
            validationErrors.add(name + INVALID_VALUE_RANGE_ERROR_MESSAGE_SUFFIX);
            return false;
        }

        return true;
    }
}
