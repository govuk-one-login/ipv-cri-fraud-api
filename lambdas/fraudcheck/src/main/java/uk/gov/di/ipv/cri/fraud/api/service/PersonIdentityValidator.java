package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.util.JsonValidationUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonIdentityValidator {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final int MIN_SUPPORTED_ADDRESSES = 1;
    public static final int MAX_SUPPORTED_ADDRESSES = 32;
    private static final int NAME_STRING_MAX_LEN = 1024;
    private static final String ADDRESSES_CHECK_ERROR =
            "Address validation error - %d addresses found %d CURRENT, %d PREVIOUS and %d INVALID.";
    private static final String ADDRESSES_INFO_MESSAGE =
            "Address validation - %d addresses found %d CURRENT, %d PREVIOUS.";

    ValidationResult<List<String>> validate(PersonIdentity personIdentity) {
        List<String> validationErrors = new ArrayList<>();

        JsonValidationUtility.validateStringDataEmptyIsFail(
                personIdentity.getFirstName(), NAME_STRING_MAX_LEN, "FirstName", validationErrors);

        JsonValidationUtility.validateStringDataEmptyIsFail(
                personIdentity.getSurname(), NAME_STRING_MAX_LEN, "Surname", validationErrors);

        if (Objects.isNull(personIdentity.getDateOfBirth())) {
            validationErrors.add(
                    "DateOfBirth" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX);
        }

        if (Objects.isNull(personIdentity.getAddresses())) {
            validationErrors.add("Addresses" + JsonValidationUtility.IS_NULL_ERROR_MESSAGE_SUFFIX);
        } else if (JsonValidationUtility.validateIntegerRangeDataNullIsFail(
                personIdentity.getAddresses().size(),
                MIN_SUPPORTED_ADDRESSES,
                MAX_SUPPORTED_ADDRESSES,
                "Addresses",
                validationErrors)) {

            // Ensure addresses can be evaluated correctly
            validationErrors.addAll(checkAddresses(personIdentity.getAddresses()));
        }

        // this implementation needs completing to validate all necessary fields
        return new ValidationResult<>(validationErrors.isEmpty(), validationErrors);
    }

    private List<String> checkAddresses(List<Address> addresses) {

        List<String> validationErrors = new ArrayList<>();

        int currentAddressCount = 0;
        int previousAddressCount = 0;
        int errorAddressCount = 0;

        for (Address address : addresses) {

            AddressType addressType = address.getAddressType();

            if (addressType == AddressType.CURRENT) {
                currentAddressCount++;
            } else if (addressType == AddressType.PREVIOUS) {
                previousAddressCount++;
            } else {
                errorAddressCount++;
            }
        }

        if ((currentAddressCount == 0) || (errorAddressCount > 0)) {
            validationErrors.add(
                    createAddressCheckErrorMessage(
                            addresses.size(),
                            currentAddressCount,
                            previousAddressCount,
                            errorAddressCount));
        } else {

            String addressInfoMessage =
                    createAddressInfoMessage(
                            addresses.size(), currentAddressCount, previousAddressCount);

            LOGGER.info(addressInfoMessage);
        }

        return validationErrors;
    }

    private String createAddressInfoMessage(
            int addressCount, int currentAddressCount, int previousAddressCount) {
        return String.format(
                ADDRESSES_INFO_MESSAGE, addressCount, currentAddressCount, previousAddressCount);
    }

    public static String createAddressCheckErrorMessage(
            int addressCount,
            int currentAddressCount,
            int previousAddressCount,
            int errorAddressCount) {
        return String.format(
                ADDRESSES_CHECK_ERROR,
                addressCount,
                currentAddressCount,
                previousAddressCount,
                errorAddressCount);
    }
}
