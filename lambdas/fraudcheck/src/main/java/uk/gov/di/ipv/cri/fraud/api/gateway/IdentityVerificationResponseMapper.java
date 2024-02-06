package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.check.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.check.PepCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.*;
import uk.gov.di.ipv.cri.fraud.api.service.IdentityVerificationInfoResponseValidator;
import uk.gov.di.ipv.cri.fraud.api.service.logger.IdentityVerificationResponseLogger;
import uk.gov.di.ipv.cri.fraud.api.service.logger.IdentityVerificationWarningsErrorsLogger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
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

    private final IdentityVerificationResponseLogger identityVerificationResponseLogger;
    private final IdentityVerificationWarningsErrorsLogger identityVerificationWarningsErrorsLogger;

    public IdentityVerificationResponseMapper(EventProbe eventProbe, ObjectMapper objectMapper) {
        this.eventProbe = eventProbe;
        identityVerificationResponseLogger = new IdentityVerificationResponseLogger(objectMapper);
        identityVerificationWarningsErrorsLogger = new IdentityVerificationWarningsErrorsLogger();
    }

    public FraudCheckResult mapFraudResponse(IdentityVerificationResponse response) {
        ResponseType responseType = response.getResponseHeader().getResponseType();

        switch (responseType) {
            case INFO:
                {
                    eventProbe.counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_INFO);

                    FraudCheckResult fraudCheckResult =
                            mapFraudInfoResponse(
                                    response, new IdentityVerificationInfoResponseValidator());

                    identityVerificationResponseLogger.logResponseFields(response);
                    identityVerificationWarningsErrorsLogger.logAnyWarningsErrors(response);

                    return fraudCheckResult;
                }
            case ERROR, WARN, WARNING:
                {
                    eventProbe.counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_ERROR);

                    ResponseHeader responseHeader = response.getResponseHeader();

                    FraudCheckResult fraudCheckResult = new FraudCheckResult();
                    fraudCheckResult.setExecutedSuccessfully(false);
                    fraudCheckResult.setErrorMessage(
                            String.format(
                                    IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                                    replaceWithDefaultErrorValueIfBlank(
                                            responseHeader.getResponseCode()),
                                    replaceWithDefaultErrorValueIfBlank(
                                            responseHeader.getResponseMessage())));

                    identityVerificationResponseLogger.logResponseFields(response);
                    identityVerificationWarningsErrorsLogger.logAnyWarningsErrors(response);

                    return fraudCheckResult;
                }
            default:
                eventProbe.counterMetric(THIRD_PARTY_FRAUD_RESPONSE_TYPE_UNKNOWN);
                throw new IllegalArgumentException(
                        "Unmapped response type encountered: " + responseType);
        }
    }

    public PepCheckResult mapPEPResponse(PEPResponse response) {
        ResponseType responseType = response.getResponseHeader().getResponseType();

        switch (responseType) {
            case INFO:
                {
                    eventProbe.counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_INFO);

                    PepCheckResult pepCheckResult =
                            mapPEPInfoResponse(
                                    response, new IdentityVerificationInfoResponseValidator());

                    identityVerificationResponseLogger.logResponseFields(response);
                    identityVerificationWarningsErrorsLogger.logAnyWarningsErrors(response);

                    return pepCheckResult;
                }
            case ERROR, WARN, WARNING:
                {
                    eventProbe.counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_ERROR);

                    ResponseHeader responseHeader = response.getResponseHeader();

                    PepCheckResult pepCheckResult = new PepCheckResult();
                    pepCheckResult.setExecutedSuccessfully(false);
                    pepCheckResult.setErrorMessage(
                            String.format(
                                    IV_ERROR_RESPONSE_ERROR_MESSAGE_FORMAT,
                                    replaceWithDefaultErrorValueIfBlank(
                                            responseHeader.getResponseCode()),
                                    replaceWithDefaultErrorValueIfBlank(
                                            responseHeader.getResponseMessage())));

                    identityVerificationResponseLogger.logResponseFields(response);
                    identityVerificationWarningsErrorsLogger.logAnyWarningsErrors(response);

                    return pepCheckResult;
                }
            default:
                eventProbe.counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_UNKNOWN);
                throw new IllegalArgumentException(
                        "Unmapped response type encountered: " + responseType);
        }
    }

    private FraudCheckResult mapFraudInfoResponse(
            IdentityVerificationResponse response,
            IdentityVerificationInfoResponseValidator infoResponseValidator) {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();

        ValidationResult<List<String>> validationResult = infoResponseValidator.validate(response);

        if (validationResult.isValid()) {
            fraudCheckResult.setExecutedSuccessfully(true);

            List<DecisionElement> decisionElements =
                    response.getClientResponsePayload().getDecisionElements();

            List<String> fraudCodes = new ArrayList<>();

            List<String> recordHistoryfields =
                    List.of(
                            "IDandLocDataAtCL_StartDateOldestPrim",
                            "IDandLocDataAtCL_StartDateOldestSec",
                            "LocDataOnlyAtCLoc_StartDateOldestPrim");
            Map<String, Integer> activityHistoryRecords = new HashMap<>();
            Integer oldestDateInMonths = null;

            for (DecisionElement decisionElement : decisionElements) {
                decisionElement.getRules().stream()
                        .map(Rule::getRuleId)
                        .filter(StringUtils::isNotBlank)
                        .sequential()
                        .collect(Collectors.toCollection(() -> fraudCodes));

                List<DataCount> dataCounts = decisionElement.getDataCounts();

                if (null != dataCounts) {
                    activityHistoryRecords =
                            dataCounts.stream()
                                    .filter(x -> recordHistoryfields.contains(x.getName()))
                                    .collect(
                                            Collectors.toMap(
                                                    DataCount::getName, DataCount::getValue));
                    for (var entry : activityHistoryRecords.entrySet()) {
                        logRecordAge(entry.getValue().toString(), entry.getKey());
                    }

                    Integer[] activityHistoryRecordValues =
                            activityHistoryRecords
                                    .values()
                                    .toArray(new Integer[activityHistoryRecords.values().size()]);
                    oldestDateInMonths = calculateOldestDateInMonths(activityHistoryRecordValues);
                    fraudCheckResult.setOldestRecordDateInMonths(oldestDateInMonths);
                }
                if (activityHistoryRecords.values().isEmpty()) {
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

    private PepCheckResult mapPEPInfoResponse(
            PEPResponse response, IdentityVerificationInfoResponseValidator infoResponseValidator) {
        PepCheckResult pepCheckResult = new PepCheckResult();

        ValidationResult<List<String>> validationResult =
                infoResponseValidator.validatePEP(response);

        if (validationResult.isValid()) {
            pepCheckResult.setExecutedSuccessfully(true);

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

            pepCheckResult.setThirdPartyFraudCodes(
                    fraudCodes.toArray(fraudCodes.toArray(String[]::new)));

            eventProbe.counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_PASS);
        } else {
            pepCheckResult.setExecutedSuccessfully(false);
            pepCheckResult.setErrorMessage(IV_INFO_RESPONSE_VALIDATION_FAILED_MSG);

            LOGGER.error(
                    () -> (IV_INFO_RESPONSE_VALIDATION_FAILED_MSG + validationResult.getError()));

            eventProbe.counterMetric(THIRD_PARTY_PEP_RESPONSE_TYPE_INFO_VALIDATION_FAIL);
        }
        pepCheckResult.setTransactionId(response.getResponseHeader().getExpRequestId());
        return pepCheckResult;
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

    private void logRecordAge(String fieldDate, String fieldName) {
        try {
            YearMonth date = YearMonth.parse(fieldDate, DateTimeFormatter.ofPattern("yyyyMM"));

            int dateInMonths =
                    Math.toIntExact(
                            ChronoUnit.MONTHS.between(
                                    YearMonth.parse(date.toString()), YearMonth.now()));

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
        } catch (DateTimeParseException e) {
            LOGGER.warn("Activity history field {} had non-date value {}", fieldName, fieldDate);
        } catch (Exception e) {
            LOGGER.warn("Invalid value in response for {}", fieldName);
        }
    }

    private Integer calculateOldestDateInMonths(Integer... recordDateValues) {

        List<Integer> dataCounts = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        try {
            String dateToday =
                    YearMonth.parse(LocalDate.now().format(formatter), formatter).toString();
            dataCounts =
                    Arrays.stream(recordDateValues)
                            .filter(e -> (e != 0)) // Filter out zeroed dates (CC2)
                            .map(x -> YearMonth.parse(x.toString(), formatter).toString())
                            .map(
                                    x ->
                                            ChronoUnit.MONTHS.between(
                                                    YearMonth.parse(x), YearMonth.parse(dateToday)))
                            .map(Math::toIntExact)
                            .toList();
        } catch (Exception e) {
            LOGGER.warn("Invalid value in response for activity history score date");
        }
        int oldestDate = 0; // Set to 0, if all dates are null then this will make the score 0
        if (!dataCounts.isEmpty()) {
            oldestDate = Collections.max(dataCounts);
        }
        return oldestDate;
    }
}
