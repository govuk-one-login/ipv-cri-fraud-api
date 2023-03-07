package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.*;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationInfoResponseValidator;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_FAIL;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_PASS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_FRAUD_RESPONSE_TYPE_UNKNOWN;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_TYPE_INFO;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_FAIL;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_PASS;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.THIRD_PARTY_PEP_RESPONSE_TYPE_UNKNOWN;

public class IdentityVerificationResponseMapper {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT =
            "Error code: %s, error description: %s";
    public static final String IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK =
            "Not specified";
    public static final String IV_INFO_RESPONSE_VALIDATION_FAILED_MSG =
            "Identity Verification Info Response failed validation.";

    private final EventProbe eventProbe;

    public IdentityVerificationResponseMapper(EventProbe eventProbe) {
        this.eventProbe = eventProbe;
    }

    public FraudCheckResult mapIdentityVerificationResponse(IdentityVerificationResponse response) {
        ResponseType responseType = response.getResponseHeader().getResponseType();

        switch (responseType) {
            case INFO:
                eventProbe.counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO);
                return mapResponse(response, new IdentityVerificationInfoResponseValidator());
            case ERROR:
            case WARN:
            case WARNING:
                eventProbe.counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR);
                return mapErrorResponse(response.getResponseHeader());
            default:
                eventProbe.counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_UNKNOWN);
                throw new IllegalArgumentException(
                        "Unmapped response type encountered: " + responseType);
        }
    }

    public FraudCheckResult mapPEPResponse(PEPResponse response) {
        ResponseType responseType = response.getResponseHeader().getResponseType();

        switch (responseType) {
            case INFO:
                eventProbe.counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_INFO);
                return mapPEPResponse(response, new IdentityVerificationInfoResponseValidator());
            case ERROR:
            case WARN:
            case WARNING:
                eventProbe.counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR);
                return mapErrorResponse(response.getResponseHeader());
            default:
                eventProbe.counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_UNKNOWN);
                throw new IllegalArgumentException(
                        "Unmapped response type encountered: " + responseType);
        }
    }

    private FraudCheckResult mapResponse(
            IdentityVerificationResponse response,
            IdentityVerificationInfoResponseValidator infoResponseValidator) {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();

        ValidationResult<List<String>> validationResult = infoResponseValidator.validate(response);

        if (validationResult.isValid()) {
            fraudCheckResult.setExecutedSuccessfully(true);

            List<DecisionElement> decisionElements =
                    response.getClientResponsePayload().getDecisionElements();

            List<String> fraudCodes = new ArrayList<>();

            for (DecisionElement decisionElement : decisionElements) {
                decisionElement.getRules().stream()
                        .map(Rule::getRuleId)
                        .filter(StringUtils::isNotBlank)
                        .sequential()
                        .collect(Collectors.toCollection(() -> fraudCodes));

                List<DataCount> dataCounts = decisionElement.getDataCounts();

                String IDandLocDataAtCL_StartDateOldestPrim = null;
                String IDandLocDataAtCL_StartDate0ldestSec = null;
                String LocDataOnlyAtCLoc_StartDate0ldestPrim = null;

                if (null != dataCounts) {
                    for (DataCount dataCountObject : dataCounts) {
                        boolean dataCountObjectNotNull =
                                null != dataCountObject.getValue()
                                        && null != dataCountObject.getName();
                        if (dataCountObjectNotNull
                                && dataCountObject
                                        .getName()
                                        .equals("IDandLocDataAtCL_StartDateOldestPrim")) {
                            Integer dateValue = dataCountObject.getValue();

                            IDandLocDataAtCL_StartDateOldestPrim = dateValue.toString();

                            try {
                                calculateRecordAge(
                                        IDandLocDataAtCL_StartDateOldestPrim,
                                        "IDandLocDataAtCL_StartDateOldestPrim");
                            } catch (Exception e) {
                                LOGGER.info(
                                        "Invalid value in reponse for IDandLocDataAtCL_StartDateOldestPrim");
                            }
                        }
                        if (dataCountObjectNotNull
                                && dataCountObject
                                        .getName()
                                        .equals("IDandLocDataAtCL_StartDate0ldestSec")) {
                            Integer dateValue = dataCountObject.getValue();

                            IDandLocDataAtCL_StartDate0ldestSec = dateValue.toString();

                            try {
                                calculateRecordAge(
                                        IDandLocDataAtCL_StartDate0ldestSec,
                                        "IDandLocDataAtCL_StartDate0ldestSec");
                            } catch (Exception e) {
                                LOGGER.info(
                                        "Invalid value in reponse for IDandLocDataAtCL_StartDate0ldestSec");
                            }
                        }
                        if (dataCountObjectNotNull
                                && dataCountObject
                                        .getName()
                                        .equals("LocDataOnlyAtCLoc_StartDate0ldestPrim")) {
                            Integer dateValue = dataCountObject.getValue();

                            LocDataOnlyAtCLoc_StartDate0ldestPrim = dateValue.toString();

                            try {
                                calculateRecordAge(
                                        LocDataOnlyAtCLoc_StartDate0ldestPrim,
                                        "LocDataOnlyAtCLoc_StartDate0ldestPrim");
                            } catch (Exception e) {
                                LOGGER.info(
                                        "Invalid value in reponse for LocDataOnlyAtCLoc_StartDate0ldestPrim");
                            }
                        }
                    }
                }
                if (null == IDandLocDataAtCL_StartDateOldestPrim
                        && null == IDandLocDataAtCL_StartDate0ldestSec
                        && null == LocDataOnlyAtCLoc_StartDate0ldestPrim) {
                    LOGGER.info("No value found for Activity History score related fields ");
                }
            }

            Integer decisionScore = getDecisionScore(decisionElements, "Fraud");

            fraudCheckResult.setThirdPartyFraudCodes(
                    fraudCodes.toArray(fraudCodes.toArray(String[]::new)));
            fraudCheckResult.setDecisionScore(String.valueOf(decisionScore));

            eventProbe.counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_PASS);
        } else {
            fraudCheckResult.setExecutedSuccessfully(false);
            fraudCheckResult.setErrorMessage(IV_INFO_RESPONSE_VALIDATION_FAILED_MSG);

            LOGGER.error(
                    () -> (IV_INFO_RESPONSE_VALIDATION_FAILED_MSG + validationResult.getError()));

            eventProbe.counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO_VALIDATION_FAIL);
        }
        fraudCheckResult.setTransactionId(response.getResponseHeader().getExpRequestId());
        return fraudCheckResult;
    }

    private FraudCheckResult mapPEPResponse(
            PEPResponse response, IdentityVerificationInfoResponseValidator infoResponseValidator) {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validatePEP(response);

        if (validationResult.isValid()) {
            fraudCheckResult.setExecutedSuccessfully(true);

            List<DecisionElement> decisionElements =
                    response.getClientResponsePayload().getDecisionElements();

            List<String> fraudCodes = new ArrayList<>();

            for (DecisionElement decisionElement : decisionElements) {
                decisionElement.getRules().stream()
                        .map(Rule::getRuleId)
                        .filter(StringUtils::isNotBlank)
                        .sequential()
                        .collect(Collectors.toCollection(() -> fraudCodes));
            }

            getDecisionScore(decisionElements, "PEP");

            fraudCheckResult.setThirdPartyFraudCodes(
                    fraudCodes.toArray(fraudCodes.toArray(String[]::new)));

            eventProbe.counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_PASS);
        } else {
            fraudCheckResult.setExecutedSuccessfully(false);
            fraudCheckResult.setErrorMessage(IV_INFO_RESPONSE_VALIDATION_FAILED_MSG);

            LOGGER.error(
                    () -> (IV_INFO_RESPONSE_VALIDATION_FAILED_MSG + validationResult.getError()));

            eventProbe.counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_FAIL);
        }
        fraudCheckResult.setTransactionId(response.getResponseHeader().getExpRequestId());
        return fraudCheckResult;
    }

    private FraudCheckResult mapErrorResponse(ResponseHeader responseHeader) {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();
        fraudCheckResult.setExecutedSuccessfully(false);
        fraudCheckResult.setErrorMessage(
                String.format(
                        IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                        replaceWithDefaultErrorValueIfBlank(responseHeader.getResponseCode()),
                        replaceWithDefaultErrorValueIfBlank(responseHeader.getResponseMessage())));
        return fraudCheckResult;
    }

    private String replaceWithDefaultErrorValueIfBlank(String input) {
        return StringUtils.isBlank(input)
                ? IV_ERROR_RESPONSE_ERROR_MESSAGE_DEFAULT_FIELD_VALUE_IF_BLANK
                : input;
    }

    private Integer getDecisionScore(List<DecisionElement> decisionElements, String checkType) {
        if (!decisionElements.isEmpty()) {
            Integer decisionScore = decisionElements.get(0).getScore();
            LOGGER.info("{} decision score = {}", checkType, decisionScore);
            return decisionScore;
        } else {
            LOGGER.info("No decision elements");
        }
        return null;
    }

    private String calculateRecordAge(String fieldDate, String fieldName) {
        LocalDate date = LocalDate.parse(fieldDate, DateTimeFormatter.ofPattern("yyddMM"));
        Duration diff = Duration.between(date.atStartOfDay(), LocalDate.now().atStartOfDay());
        long diffDays = diff.toDays();
        long dateInMonths = diffDays / 12;
        String approxDateInYears = "No value present";

        if (dateInMonths >= 24) {
            approxDateInYears = "Greater than 2";
        }
        if (dateInMonths >= 6 && dateInMonths < 24) {
            approxDateInYears = "Greater than minimum and less than 2";
        }
        if (dateInMonths < 6) {
            approxDateInYears = "Less than minimum";
        }

        LOGGER.info(
                "Logging activity history score related value in response {} {}",
                fieldName,
                approxDateInYears);
        return approxDateInYears;
    }
}
