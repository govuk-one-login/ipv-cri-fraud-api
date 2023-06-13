package uk.gov.di.ipv.cri.fraud.api.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.di.ipv.cri.fraud.api.domain.Evidence;
import uk.gov.di.ipv.cri.fraud.api.domain.checkdetails.Check;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.di.ipv.cri.fraud.api.domain.EvidenceType.IDENTITY_CHECK;
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

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem, true);

        assertEquals(evidence.getType(), IDENTITY_CHECK.toString());
        assertEquals(evidence.getTxn(), fraudResultItem.getTransactionId());
        assertEquals(evidence.getIdentityFraudScore(), fraudResultItem.getIdentityFraudScore());
        assertEquals(evidence.getCi(), fraudResultItem.getContraIndicators());
        assertEquals(evidence.getDecisionScore(), fraudResultItem.getDecisionScore());
        assertEquals(evidence.getActivityHistoryScore(), fraudResultItem.getActivityHistoryScore());
    }

    @Test
    void TestFraudResultItemISuccessfullyMappedToEvidenceWithoutActivityHistoryIfToggleSet() {

        FraudResultItem fraudResultItem = new FraudResultItem();

        fraudResultItem.setIdentityFraudScore(1);
        fraudResultItem.setContraIndicators(List.of("u101"));
        fraudResultItem.setSessionId(UUID.randomUUID());
        fraudResultItem.setTransactionId("01");
        fraudResultItem.setActivityHistoryScore(1);
        fraudResultItem.setActivityFrom("1992-12-11");

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem, false);

        assertEquals(evidence.getType(), IDENTITY_CHECK.toString());
        assertEquals(evidence.getTxn(), fraudResultItem.getTransactionId());
        assertEquals(evidence.getIdentityFraudScore(), fraudResultItem.getIdentityFraudScore());
        assertEquals(evidence.getCi(), fraudResultItem.getContraIndicators());
        assertEquals(evidence.getDecisionScore(), fraudResultItem.getDecisionScore());
        assertNotEquals(
                evidence.getActivityHistoryScore(), fraudResultItem.getActivityHistoryScore());
        assertNull(evidence.getActivityHistoryScore());
        assertNull(evidence.getCheckDetails());
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
            fraudResultItem.setFailedCheckDetails(
                    List.of(MORTALITY_CHECK.toString().toLowerCase()));
        } else {
            fraudResultItem.setCheckDetails(List.of(MORTALITY_CHECK.toString().toLowerCase()));
        }

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem, true);

        Check mortalityCheck = new Check(MORTALITY_CHECK.toString().toLowerCase());

        assertEquals(evidence.getType(), IDENTITY_CHECK.toString());
        assertEquals(evidence.getTxn(), fraudResultItem.getTransactionId());
        assertEquals(evidence.getIdentityFraudScore(), fraudResultItem.getIdentityFraudScore());
        assertEquals(evidence.getCi(), fraudResultItem.getContraIndicators());
        assertEquals(evidence.getDecisionScore(), fraudResultItem.getDecisionScore());
        assertEquals(evidence.getActivityHistoryScore(), fraudResultItem.getActivityHistoryScore());

        if (checkType.equals("failedCheck")) {
            assertEquals(evidence.getFailedCheckDetails().get(0), mortalityCheck);
        } else {
            assertEquals(evidence.getCheckDetails().get(0), mortalityCheck);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"check", "failedCheck"})
    void TestFraudResultItemISuccessfullyMappedToEvidenceWithChecksWithActivityHistoryScore(
            String checkType) {

        FraudResultItem fraudResultItem = new FraudResultItem();

        fraudResultItem.setIdentityFraudScore(1);
        fraudResultItem.setContraIndicators(List.of("u101"));
        fraudResultItem.setSessionId(UUID.randomUUID());
        fraudResultItem.setTransactionId("01");
        fraudResultItem.setActivityHistoryScore(1);
        fraudResultItem.setActivityFrom("1992-12-11");

        if (checkType.equals("failedCheck")) {
            fraudResultItem.setFailedCheckDetails(
                    List.of(MORTALITY_CHECK.toString().toLowerCase()));
        } else {
            fraudResultItem.setCheckDetails(List.of(MORTALITY_CHECK.toString().toLowerCase()));
        }

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem, true);

        Check activityHistoryCheck = new Check();
        activityHistoryCheck.setIdentityCheckPolicy("none");
        activityHistoryCheck.setActivityFrom("1992-12-11");

        assertEquals(evidence.getType(), IDENTITY_CHECK.toString());
        assertEquals(evidence.getTxn(), fraudResultItem.getTransactionId());
        assertEquals(evidence.getIdentityFraudScore(), fraudResultItem.getIdentityFraudScore());
        assertEquals(evidence.getCi(), fraudResultItem.getContraIndicators());
        assertEquals(evidence.getDecisionScore(), fraudResultItem.getDecisionScore());
        assertEquals(evidence.getActivityHistoryScore(), fraudResultItem.getActivityHistoryScore());

        List<Check> checkDetails = evidence.getCheckDetails();
        if (checkType.equals("failedCheck")) {
            checkDetails = evidence.getFailedCheckDetails();
        }
        for (Check check : checkDetails) {
            if (null == check.getFraudCheck()) {
                assertEquals(check, activityHistoryCheck);
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
            fraudResultItem.setFailedCheckDetails(
                    List.of(IMPERSONATION_RISK_CHECK.toString().toLowerCase()));
        } else {
            fraudResultItem.setCheckDetails(
                    List.of(IMPERSONATION_RISK_CHECK.toString().toLowerCase()));
        }

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem, true);

        Check impersonationRiskCheck = new Check(IMPERSONATION_RISK_CHECK.toString().toLowerCase());
        impersonationRiskCheck.setTxn("02");

        assertEquals(evidence.getType(), IDENTITY_CHECK.toString());
        assertEquals(evidence.getTxn(), fraudResultItem.getTransactionId());
        assertEquals(evidence.getIdentityFraudScore(), fraudResultItem.getIdentityFraudScore());
        assertEquals(evidence.getCi(), fraudResultItem.getContraIndicators());
        assertEquals(evidence.getDecisionScore(), fraudResultItem.getDecisionScore());

        if (checkType.equals("failedCheck")) {
            assertEquals(evidence.getFailedCheckDetails().get(0), impersonationRiskCheck);
        } else {
            assertEquals(evidence.getCheckDetails().get(0), impersonationRiskCheck);
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
                            IMPERSONATION_RISK_CHECK.toString().toLowerCase(),
                            MORTALITY_CHECK.toString().toLowerCase()));
        } else {
            fraudResultItem.setCheckDetails(
                    List.of(
                            IMPERSONATION_RISK_CHECK.toString().toLowerCase(),
                            MORTALITY_CHECK.toString().toLowerCase()));
        }

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem, true);

        Check impersonationRiskCheck = new Check(IMPERSONATION_RISK_CHECK.toString().toLowerCase());
        impersonationRiskCheck.setTxn("02");

        Check activityHistoryCheck = new Check();
        activityHistoryCheck.setIdentityCheckPolicy("none");
        activityHistoryCheck.setActivityFrom("1992-12-11");

        assertEquals(evidence.getType(), IDENTITY_CHECK.toString());
        assertEquals(evidence.getTxn(), fraudResultItem.getTransactionId());
        assertEquals(evidence.getIdentityFraudScore(), fraudResultItem.getIdentityFraudScore());
        assertEquals(evidence.getCi(), fraudResultItem.getContraIndicators());
        assertEquals(evidence.getDecisionScore(), fraudResultItem.getDecisionScore());
        assertEquals(evidence.getActivityHistoryScore(), fraudResultItem.getActivityHistoryScore());

        List<Check> checkDetails = evidence.getCheckDetails();
        if (checkType.equals("failedCheck")) {
            assertEquals(evidence.getFailedCheckDetails().size(), 3);
            checkDetails = evidence.getFailedCheckDetails();
        }
        for (Check check : checkDetails) {
            if (null == check.getFraudCheck()) {
                assertEquals(check, activityHistoryCheck);
            } else {
                if (check.getFraudCheck().equalsIgnoreCase(IMPERSONATION_RISK_CHECK.toString())) {
                    assertEquals(check, impersonationRiskCheck);
                } else {
                    assertEquals(check.getFraudCheck(), MORTALITY_CHECK.toString().toLowerCase());
                    assertEquals(check.getCheckMethod(), "data");
                    assertNull(check.getTxn());
                    assertNull(check.getIdentityCheckPolicy());
                }
            }
        }
    }
}
