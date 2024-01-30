package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.WarningsErrors;

import java.util.List;

public class IdentityVerificationWarningsErrorsLogger {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String CANNOT_LOCATE_WARNINGS_ERRORS =
            "Cannot locate Warnings/Errors as response had null entries";

    public static final String NO_WARNINGS_ERRORS_FORMAT =
            "%s - did not contain Warnings/Errors array";

    public static final String NULL_WARNINGS_ERRORS_ELEMENT_FORMAT =
            "%s - Warnings/Errors array had an null element";

    private static final String WARNINGS_ERRORS_LOG_FORMAT =
            "%s - DecisionElement:ResponseType:%s listed in WarningsErrors, ResponseCode:%s, ResponseMessage:%s";

    /** This class is intended log only with no impact on control flow. */
    public IdentityVerificationWarningsErrorsLogger() {
        /* No Args */
    }

    public void logAnyWarningsErrors(IdentityVerificationResponse response) {
        if (null != response
                && null != response.getClientResponsePayload()
                && null != response.getClientResponsePayload().getDecisionElements()
                && null != response.getClientResponsePayload().getDecisionElements().get(0)) {

            String logLinePrefix = getLogLinePrefix(response);

            List<WarningsErrors> warningsErrors =
                    response.getClientResponsePayload()
                            .getDecisionElements()
                            .get(0)
                            .getWarningsErrors();

            processWarningsErrors(logLinePrefix, warningsErrors);

        } else {
            LOGGER.error(CANNOT_LOCATE_WARNINGS_ERRORS);
        }
    }

    private String getLogLinePrefix(IdentityVerificationResponse response) {
        String responseType = null;
        if (null != response.getResponseHeader()
                && null != response.getResponseHeader().getResponseType()) {
            responseType = String.valueOf(response.getResponseHeader().getResponseType());
        }

        String requestType = null;
        if (null != response.getResponseHeader()
                && null != response.getResponseHeader().getRequestType()) {
            requestType = response.getResponseHeader().getRequestType();
        }

        return String.format("%s - %s", requestType, responseType);
    }

    private void processWarningsErrors(String logLinePrefix, List<WarningsErrors> warningsErrors) {
        // Responses where WarningsErrors is null or empty are expected
        if (null != warningsErrors && !warningsErrors.isEmpty()) {
            for (WarningsErrors warningError : warningsErrors) {
                processWarningError(logLinePrefix, warningError);
            }
        } else {
            String logMessage = String.format(NO_WARNINGS_ERRORS_FORMAT, logLinePrefix);
            LOGGER.info(logMessage);
        }
    }

    private void processWarningError(String logLinePrefix, WarningsErrors warningError) {
        if (null != warningError) {
            String responseType = warningError.getResponseType();
            String responseMessage = warningError.getResponseMessage();
            String responseCode = warningError.getResponseCode();

            String logMessage =
                    String.format(
                            WARNINGS_ERRORS_LOG_FORMAT,
                            logLinePrefix,
                            responseType,
                            responseCode,
                            responseMessage);

            LOGGER.warn(logMessage);
        } else {
            // Not expected to happen but logged if it does
            String logMessage = String.format(NULL_WARNINGS_ERRORS_ELEMENT_FORMAT, logLinePrefix);
            LOGGER.warn(logMessage);
        }
    }
}
