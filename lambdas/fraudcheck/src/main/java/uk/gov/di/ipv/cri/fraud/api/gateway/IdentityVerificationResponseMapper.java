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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.toIntExact;
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

                OtherData otherData = decisionElement.getOtherData();
                Boolean isOtherDataNull = isOtherDataNull(otherData);

                if (!isOtherDataNull) {
                    AuthConsumer authConsumer =
                            otherData.getAuthResults().getAuthPlusResults().getAuthConsumer();
                    String LocDataOldestPrim = null;
                    String idAndLocDataOldestPrim = null;
                    String idAndLocDataOldestSec = null;
                    if (null != authConsumer.getLocDataOnlyAtCLoc()
                            && null
                                    != authConsumer
                                            .getLocDataOnlyAtCLoc()
                                            .getStartDateOldestPrim()) {
                        LocDataOldestPrim =
                                calculateOldestDataRecordDate(
                                        authConsumer
                                                .getLocDataOnlyAtCLoc()
                                                .getStartDateOldestPrim(),
                                        "LocDataOnlyAtCLoc_StartDate0ldestPrim");
                    }

                    if (null != authConsumer.getIdandLocDataAtCL()
                            && null
                                    != authConsumer
                                            .getIdandLocDataAtCL()
                                            .getStartDateOldestPrim()) {
                        idAndLocDataOldestPrim =
                                calculateOldestDataRecordDate(
                                        authConsumer.getIdandLocDataAtCL().getStartDateOldestPrim(),
                                        "idAndLocDataAtCL_StartDateOldestPrim");
                    }

                    if (null != authConsumer.getIdandLocDataAtCL()
                            && null != authConsumer.getIdandLocDataAtCL().getStartDateOldestSec()) {
                        idAndLocDataOldestSec =
                                calculateOldestDataRecordDate(
                                        authConsumer.getIdandLocDataAtCL().getStartDateOldestSec(),
                                        "idAndLocDataAtCL_StartDateOldestSec");
                    }
                    String oldestRecord =
                            calculateOldestDateCount(
                                    LocDataOldestPrim,
                                    idAndLocDataOldestPrim,
                                    idAndLocDataOldestSec);
                    fraudCheckResult.setOldestRecordDate(oldestRecord);
                } else {
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

                OtherData otherData = decisionElement.getOtherData();
                Boolean isOtherDataNull = isOtherDataNull(otherData);

                if (!isOtherDataNull) {
                    AuthConsumer authConsumer =
                            otherData.getAuthResults().getAuthPlusResults().getAuthConsumer();
                    String LocDataOldestPrim = null;
                    String idAndLocDataOldestPrim = null;
                    String idAndLocDataOldestSec = null;
                    if (null != authConsumer.getLocDataOnlyAtCLoc()
                            && null
                                    != authConsumer
                                            .getLocDataOnlyAtCLoc()
                                            .getStartDateOldestPrim()) {
                        LocDataOldestPrim =
                                calculateOldestDataRecordDate(
                                        authConsumer
                                                .getLocDataOnlyAtCLoc()
                                                .getStartDateOldestPrim(),
                                        "LocDataOnlyAtCLoc_StartDate0ldestPrim");
                    }

                    if (null != authConsumer.getIdandLocDataAtCL()
                            && null
                                    != authConsumer
                                            .getIdandLocDataAtCL()
                                            .getStartDateOldestPrim()) {
                        idAndLocDataOldestPrim =
                                calculateOldestDataRecordDate(
                                        authConsumer.getIdandLocDataAtCL().getStartDateOldestPrim(),
                                        "idAndLocDataAtCL_StartDateOldestPrim");
                    }

                    if (null != authConsumer.getIdandLocDataAtCL()
                            && null != authConsumer.getIdandLocDataAtCL().getStartDateOldestSec()) {
                        idAndLocDataOldestSec =
                                calculateOldestDataRecordDate(
                                        authConsumer.getIdandLocDataAtCL().getStartDateOldestSec(),
                                        "idAndLocDataAtCL_StartDateOldestSec");
                    }
                    String oldestRecord =
                            calculateOldestDateCount(
                                    LocDataOldestPrim,
                                    idAndLocDataOldestPrim,
                                    idAndLocDataOldestSec);
                    fraudCheckResult.setOldestRecordDate(oldestRecord);
                } else {
                    LOGGER.info("No value found for Activity History score related fields ");
                }
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

    // TODO: Uncomment for score calculation ticket
    private String calculateOldestDataRecordDate(String fieldDate, String fieldName) {
        String LocDataOnlyAtCLoc_StartDate0ldestPrim = "No value present";
        LocalDate date = LocalDate.parse(fieldDate, DateTimeFormatter.ofPattern("yyddMM"));
        Duration diff = Duration.between(LocalDate.now().atStartOfDay(), date.atStartOfDay());
        long diffDays = diff.toDays();
        long dateInYears = diffDays / 365;
        String approxDateInYears = null;
        if (dateInYears > 1) {
            approxDateInYears = "Greater than 1";
        }
        if (dateInYears > 3) {
            approxDateInYears = "Greater than 3";
        }
        LocDataOnlyAtCLoc_StartDate0ldestPrim = approxDateInYears;

        // TODO: remove activity history score field logging after calculator is made and
        // score
        // added to VC

        LOGGER.info(
                "Logging activity history score related value in response {} {}",
                fieldName,
                LocDataOnlyAtCLoc_StartDate0ldestPrim);
        return LocDataOnlyAtCLoc_StartDate0ldestPrim;
    }

    private Boolean isOtherDataNull(OtherData otherData) {
        if (null != otherData) {
            AuthResults authResults = otherData.getAuthResults();
            if (null != authResults) {
                AuthPlusResults authPlusResults = authResults.getAuthPlusResults();
                if (null != authPlusResults) {
                    AuthConsumer authConsumer = authPlusResults.getAuthConsumer();
                    if (null != authConsumer) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private String calculateOldestDateCount(
            String iDAndLocDataAtCL_StartDateOldestPrim,
            String iDAndLocDataAtCL_StartDateOldestSec,
            String LocDataOnlyAtCLoc_StartDateOldestPrim) {

        List<Integer> dataCounts = new ArrayList<>();
        int idAndLocOldestPrimeDayDifferenceAsInt = 0;
        int idAndLocOldestSecDayDifferenceAsInt = 0;
        int LocOnlyOldestPrimeDayDifferenceAsInt = 0;

        if (null != iDAndLocDataAtCL_StartDateOldestPrim) {
            LocalDate iDAndLocDataAtCL_StartDateOldestPrimAsDate =
                    LocalDate.parse(
                            iDAndLocDataAtCL_StartDateOldestPrim,
                            DateTimeFormatter.ofPattern("yyddMM"));
            Duration diff1 =
                    Duration.between(
                            LocalDate.now().atStartOfDay(),
                            iDAndLocDataAtCL_StartDateOldestPrimAsDate.atStartOfDay());
            long diff1Days = diff1.toDays();
            idAndLocOldestPrimeDayDifferenceAsInt = toIntExact(diff1Days);
            dataCounts.add(idAndLocOldestPrimeDayDifferenceAsInt);
        }
        if (null != iDAndLocDataAtCL_StartDateOldestSec) {
            LocalDate iDAndLocDataAtCL_StartDateOldestSecAsDate =
                    LocalDate.parse(
                            iDAndLocDataAtCL_StartDateOldestSec,
                            DateTimeFormatter.ofPattern("yyddMM"));
            Duration diff2 =
                    Duration.between(
                            LocalDate.now().atStartOfDay(),
                            iDAndLocDataAtCL_StartDateOldestSecAsDate.atStartOfDay());
            long diff2Days = diff2.toDays();
            idAndLocOldestSecDayDifferenceAsInt = toIntExact(diff2Days);
            dataCounts.add(idAndLocOldestSecDayDifferenceAsInt);
        }
        if (null != LocDataOnlyAtCLoc_StartDateOldestPrim) {
            LocalDate LocDataOnlyAtCLoc_StartDateOldestPrimAsDate =
                    LocalDate.parse(
                            LocDataOnlyAtCLoc_StartDateOldestPrim,
                            DateTimeFormatter.ofPattern("yyddMM"));
            Duration diff3 =
                    Duration.between(
                            LocalDate.now().atStartOfDay(),
                            LocDataOnlyAtCLoc_StartDateOldestPrimAsDate.atStartOfDay());
            long diff3Days = diff3.toDays();

            LocOnlyOldestPrimeDayDifferenceAsInt = toIntExact(diff3Days);
            dataCounts.add(LocOnlyOldestPrimeDayDifferenceAsInt);
        }
        int oldestDate =
                1; // Setting oldestDate default value to 1 so that there are no matches in below if
        // statement
        // if all values are null
        if (dataCounts.size() > 0) {
            oldestDate = Collections.max(dataCounts);
        }
        if (oldestDate == idAndLocOldestPrimeDayDifferenceAsInt) {
            return iDAndLocDataAtCL_StartDateOldestPrim;

        } else if (oldestDate == idAndLocOldestSecDayDifferenceAsInt) {
            return iDAndLocDataAtCL_StartDateOldestSec;

        } else if (oldestDate == LocOnlyOldestPrimeDayDifferenceAsInt) {
            return LocDataOnlyAtCLoc_StartDateOldestPrim;
        }
        return null;
    }
}
