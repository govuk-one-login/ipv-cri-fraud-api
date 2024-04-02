package uk.gov.di.ipv.cri.fraud.api.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.di.ipv.cri.fraud.api.domain.Evidence;
import uk.gov.di.ipv.cri.fraud.api.domain.checkdetails.Check;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.di.ipv.cri.fraud.api.domain.EvidenceType.IDENTITY_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.ACTIVITY_HISTORY_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.IMPERSONATION_RISK_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.MORTALITY_CHECK;

class EvidenceHelperTest {

    @Test
    void TestFraudResultItemISuccessfullyMappedToEvidence() {

        FraudResultItem fraudResultItem = new FraudResultItem();

        fraudResultItem.setIdentityFraudScore(1);
        fraudResultItem.setContraIndicators(List.of("u101"));
        fraudResultItem.setSessionId(UUID.randomUUID());
        fraudResultItem.setTransactionId("01");

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem);

        assertEquals(IDENTITY_CHECK.toString(), evidence.getType());
        assertEquals(fraudResultItem.getTransactionId(), evidence.getTxn());
        assertEquals(fraudResultItem.getIdentityFraudScore(), evidence.getIdentityFraudScore());
        assertEquals(fraudResultItem.getContraIndicators(), evidence.getCi());
        assertEquals(fraudResultItem.getDecisionScore(), evidence.getDecisionScore());
        assertEquals(fraudResultItem.getActivityHistoryScore(), evidence.getActivityHistoryScore());
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "empty"})
    void TestFraudResultItemISuccessfullyMappedToEvidenceWhenChecksAreNullOrEmpty(String scenario) {

        FraudResultItem fraudResultItem = new FraudResultItem();

        fraudResultItem.setIdentityFraudScore(1);
        fraudResultItem.setContraIndicators(List.of("u101"));
        fraudResultItem.setSessionId(UUID.randomUUID());
        fraudResultItem.setTransactionId("01");

        if ("empty".equals(scenario)) {
            fraudResultItem.setCheckDetails(List.of());
            fraudResultItem.setFailedCheckDetails(List.of());
        } else {
            fraudResultItem.setCheckDetails(null);
            fraudResultItem.setFailedCheckDetails(null);
        }

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem);

        assertEquals(IDENTITY_CHECK.toString(), evidence.getType());
        assertEquals(fraudResultItem.getTransactionId(), evidence.getTxn());
        assertEquals(fraudResultItem.getIdentityFraudScore(), evidence.getIdentityFraudScore());
        assertEquals(fraudResultItem.getContraIndicators(), evidence.getCi());
        assertEquals(fraudResultItem.getDecisionScore(), evidence.getDecisionScore());
        assertEquals(fraudResultItem.getActivityHistoryScore(), evidence.getActivityHistoryScore());
    }

    @ParameterizedTest
    @ValueSource(strings = {"check", "failedCheck"})
    void TestFraudResultItemISuccessfullyMappedToEvidenceWithChecks(String checkType) {

        FraudResultItem fraudResultItem = new FraudResultItem();

        fraudResultItem.setIdentityFraudScore(1);
        fraudResultItem.setContraIndicators(List.of("u101"));
        fraudResultItem.setSessionId(UUID.randomUUID());
        fraudResultItem.setTransactionId("01");

        if (checkType.equals("failedCheck")) {
            fraudResultItem.setFailedCheckDetails(List.of(MORTALITY_CHECK.toString()));
        } else {
            fraudResultItem.setCheckDetails(List.of(MORTALITY_CHECK.toString()));
        }

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem);

        Check mortalityCheck = new Check(MORTALITY_CHECK);

        assertEquals(IDENTITY_CHECK.toString(), evidence.getType());
        assertEquals(fraudResultItem.getTransactionId(), evidence.getTxn());
        assertEquals(fraudResultItem.getIdentityFraudScore(), evidence.getIdentityFraudScore());
        assertEquals(fraudResultItem.getContraIndicators(), evidence.getCi());
        assertEquals(fraudResultItem.getDecisionScore(), evidence.getDecisionScore());
        assertEquals(fraudResultItem.getActivityHistoryScore(), evidence.getActivityHistoryScore());

        if (checkType.equals("failedCheck")) {
            assertEquals(
                    mortalityCheck.getFraudCheck(),
                    evidence.getFailedCheckDetails().get(0).getFraudCheck());
        } else {
            assertEquals(
                    mortalityCheck.getFraudCheck(),
                    evidence.getCheckDetails().get(0).getFraudCheck());
        }
    }

    @ParameterizedTest
    @CsvSource({"check, 0", "check, 1", "failedCheck, 0 ", "failedCheck, 1 "})
    void TestFraudResultItemISuccessfullyMappedToEvidenceWithChecksWithActivityHistoryScore(
            String checkType, int activityHistoryScore) {

        FraudResultItem fraudResultItem = new FraudResultItem();

        fraudResultItem.setIdentityFraudScore(1);
        fraudResultItem.setContraIndicators(List.of("u101"));
        fraudResultItem.setSessionId(UUID.randomUUID());
        fraudResultItem.setTransactionId("01");
        fraudResultItem.setActivityHistoryScore(activityHistoryScore);
        fraudResultItem.setActivityFrom("1992-12-11");

        if (checkType.equals("failedCheck")) {
            fraudResultItem.setFailedCheckDetails(List.of(ACTIVITY_HISTORY_CHECK.toString()));
        } else {
            fraudResultItem.setCheckDetails(List.of(ACTIVITY_HISTORY_CHECK.toString()));
        }

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem);

        Check activityHistoryCheck = new Check(ACTIVITY_HISTORY_CHECK);
        activityHistoryCheck.setIdentityCheckPolicy("none");
        activityHistoryCheck.setActivityFrom("1992-12-11");
        activityHistoryCheck.clearFraudCheckField(); // Required  not present

        assertEquals(IDENTITY_CHECK.toString(), evidence.getType());
        assertEquals(fraudResultItem.getTransactionId(), evidence.getTxn());
        assertEquals(fraudResultItem.getIdentityFraudScore(), evidence.getIdentityFraudScore());
        assertEquals(fraudResultItem.getContraIndicators(), evidence.getCi());
        assertEquals(fraudResultItem.getDecisionScore(), evidence.getDecisionScore());
        assertEquals(fraudResultItem.getActivityHistoryScore(), evidence.getActivityHistoryScore());

        List<Check> checkDetails = evidence.getCheckDetails();
        if (checkType.equals("failedCheck")) {
            checkDetails = evidence.getFailedCheckDetails();
        }

        if (activityHistoryScore > 0) {
            for (Check check : checkDetails) {
                if (null == check.getFraudCheck()) {
                    assertEquals(activityHistoryCheck, check);
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"check", "failedCheck"})
    void TestFraudResultItemISuccessfullyMappedToEvidenceWithChecksWithImpersonationCheck(
            String checkType) {

        FraudResultItem fraudResultItem = new FraudResultItem();

        fraudResultItem.setIdentityFraudScore(1);
        fraudResultItem.setContraIndicators(List.of("u101"));
        fraudResultItem.setSessionId(UUID.randomUUID());
        fraudResultItem.setTransactionId("01");
        fraudResultItem.setPepTransactionId("02");
        fraudResultItem.setActivityHistoryScore(1);
        fraudResultItem.setActivityFrom("1992-12-11");

        if (checkType.equals("failedCheck")) {
            fraudResultItem.setFailedCheckDetails(List.of(IMPERSONATION_RISK_CHECK.toString()));
        } else {
            fraudResultItem.setCheckDetails(List.of(IMPERSONATION_RISK_CHECK.toString()));
        }

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem);

        Check impersonationRiskCheck = new Check(IMPERSONATION_RISK_CHECK);
        impersonationRiskCheck.setTxn("02");

        assertEquals(IDENTITY_CHECK.toString(), evidence.getType());
        assertEquals(fraudResultItem.getTransactionId(), evidence.getTxn());
        assertEquals(fraudResultItem.getIdentityFraudScore(), evidence.getIdentityFraudScore());
        assertEquals(fraudResultItem.getContraIndicators(), evidence.getCi());
        assertEquals(fraudResultItem.getDecisionScore(), evidence.getDecisionScore());

        if (checkType.equals("failedCheck")) {
            assertEquals(impersonationRiskCheck, evidence.getFailedCheckDetails().get(0));
        } else {
            assertEquals(impersonationRiskCheck, evidence.getCheckDetails().get(0));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"check", "failedCheck"})
    void
            TestFraudResultItemISuccessfullyMappedToEvidenceWithChecksWithImpersonationCheckAndActivityHistoryScore(
                    String checkType) {

        FraudResultItem fraudResultItem = new FraudResultItem();

        fraudResultItem.setIdentityFraudScore(1);
        fraudResultItem.setContraIndicators(List.of("u101"));
        fraudResultItem.setSessionId(UUID.randomUUID());
        fraudResultItem.setTransactionId("01");
        fraudResultItem.setPepTransactionId("02");
        fraudResultItem.setActivityHistoryScore(1);
        fraudResultItem.setActivityFrom("1992-12-11");

        if (checkType.equals("failedCheck")) {
            fraudResultItem.setFailedCheckDetails(
                    List.of(
                            IMPERSONATION_RISK_CHECK.toString(),
                            MORTALITY_CHECK.toString(),
                            ACTIVITY_HISTORY_CHECK.toString()));
        } else {
            fraudResultItem.setCheckDetails(
                    List.of(IMPERSONATION_RISK_CHECK.toString(), MORTALITY_CHECK.toString()));
        }

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem);

        Check impersonationRiskCheck = new Check(IMPERSONATION_RISK_CHECK);
        impersonationRiskCheck.setTxn("02");

        Check activityHistoryCheck = new Check(ACTIVITY_HISTORY_CHECK);
        activityHistoryCheck.setIdentityCheckPolicy("none");
        activityHistoryCheck.setActivityFrom("1992-12-11");
        activityHistoryCheck.clearFraudCheckField(); // required not present

        assertEquals(IDENTITY_CHECK.toString(), evidence.getType());
        assertEquals(fraudResultItem.getTransactionId(), evidence.getTxn());
        assertEquals(fraudResultItem.getIdentityFraudScore(), evidence.getIdentityFraudScore());
        assertEquals(fraudResultItem.getContraIndicators(), evidence.getCi());
        assertEquals(fraudResultItem.getDecisionScore(), evidence.getDecisionScore());
        assertEquals(fraudResultItem.getActivityHistoryScore(), evidence.getActivityHistoryScore());

        List<Check> checkDetails = evidence.getCheckDetails();
        if (checkType.equals("failedCheck")) {
            assertEquals(3, evidence.getFailedCheckDetails().size());
            checkDetails = evidence.getFailedCheckDetails();
        }
        for (Check check : checkDetails) {
            if (null == check.getFraudCheck()) {
                assertEquals(activityHistoryCheck.getActivityFrom(), check.getActivityFrom());
                assertEquals(activityHistoryCheck.getCheckMethod(), check.getCheckMethod());
                assertEquals(
                        activityHistoryCheck.getActivityFrom(), fraudResultItem.getActivityFrom());
                assertEquals("none", activityHistoryCheck.getIdentityCheckPolicy());
            } else {
                if (check.getFraudCheck().equalsIgnoreCase(IMPERSONATION_RISK_CHECK.toString())) {
                    assertNotNull(check.getTxn());
                } else {
                    assertEquals(MORTALITY_CHECK.toString().toLowerCase(), check.getFraudCheck());
                    assertEquals("data", check.getCheckMethod());
                    assertNull(check.getTxn());
                    assertNull(check.getIdentityCheckPolicy());
                }
            }
        }
    }
}
