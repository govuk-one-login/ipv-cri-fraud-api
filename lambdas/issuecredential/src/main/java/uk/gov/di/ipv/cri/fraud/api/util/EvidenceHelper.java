package uk.gov.di.ipv.cri.fraud.api.util;

import uk.gov.di.ipv.cri.fraud.api.domain.Evidence;
import uk.gov.di.ipv.cri.fraud.api.domain.EvidenceType;
import uk.gov.di.ipv.cri.fraud.api.domain.checkdetails.Check;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.IMPERSONATION_RISK_CHECK;

public class EvidenceHelper {

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

            // EnumValue to String lowercase (VC)
            Check check = new Check(checkName.toLowerCase());

            // IPR check has the transaction recorded in the check result
            if (checkName.equals(IMPERSONATION_RISK_CHECK.toString())) {
                check.setTxn(resultItem.getPepTransactionId());
            }

            checkList.add(check);
        }

        return checkList;
    }
}
