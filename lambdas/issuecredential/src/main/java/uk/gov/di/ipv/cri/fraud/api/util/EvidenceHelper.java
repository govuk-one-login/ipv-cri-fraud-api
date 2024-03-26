package uk.gov.di.ipv.cri.fraud.api.util;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.fraud.api.domain.Evidence;
import uk.gov.di.ipv.cri.fraud.api.domain.EvidenceType;
import uk.gov.di.ipv.cri.fraud.api.domain.checkdetails.Check;
import uk.gov.di.ipv.cri.fraud.library.domain.CheckType;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

import java.util.ArrayList;
import java.util.List;

public class EvidenceHelper {

    @ExcludeFromGeneratedCoverageReport
    private EvidenceHelper() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static Evidence fraudCheckResultItemToEvidence(FraudResultItem fraudResultItem) {

        Evidence evidence = new Evidence();

        evidence.setType(EvidenceType.IDENTITY_CHECK.toString());
        evidence.setTxn(fraudResultItem.getTransactionId());
        evidence.setIdentityFraudScore(fraudResultItem.getIdentityFraudScore());
        evidence.setCi(fraudResultItem.getContraIndicators());
        evidence.setDecisionScore(fraudResultItem.getDecisionScore());

        evidence.setActivityHistoryScore(fraudResultItem.getActivityHistoryScore());

        List<String> stringCheckDetails = fraudResultItem.getCheckDetails();
        if (stringCheckDetails != null && !stringCheckDetails.isEmpty()) {
            evidence.setCheckDetails(createCheckList(stringCheckDetails, fraudResultItem));
        }

        List<String> stringFailedCheckDetails = fraudResultItem.getFailedCheckDetails();
        if (stringFailedCheckDetails != null && !stringFailedCheckDetails.isEmpty()) {
            evidence.setFailedCheckDetails(
                    createCheckList(stringFailedCheckDetails, fraudResultItem));
        }

        return evidence;
    }

    private static List<Check> createCheckList(
            List<String> stringChecks, FraudResultItem resultItem) {

        List<Check> checkList = new ArrayList<>();

        for (String checkName : stringChecks) {
            Check check = createCheck(checkName, resultItem);
            checkList.add(check);
        }

        return checkList;
    }

    private static Check createCheck(String checkName, FraudResultItem resultItem) {

        // Designed to fail here if checkName not valid CheckType
        CheckType checkType = CheckType.valueOf(checkName);

        Check check = new Check(checkType);

        switch (checkType) {
            case MORTALITY_CHECK, IDENTITY_THEFT_CHECK, SYNTHETIC_IDENTITY_CHECK -> {
                // No Unique handling
            }
                // IPR check has the transaction recorded in the check result
            case IMPERSONATION_RISK_CHECK -> check.setTxn(resultItem.getPepTransactionId());
            case ACTIVITY_HISTORY_CHECK -> {
                // For activity history the fraud check field is not present
                check.clearFraudCheckField();

                // Only add scores greater than 0
                // Remove restriction on 0, if we want 0 to appear in checksFailed
                if (resultItem.getActivityHistoryScore() != 0) {
                    check.setActivityFrom(resultItem.getActivityFrom());
                    check.setIdentityCheckPolicy("none");
                }
            }
        }

        return check;
    }
}
