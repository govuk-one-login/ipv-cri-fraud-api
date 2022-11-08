package uk.gov.di.ipv.cri.fraud.api.util;

import uk.gov.di.ipv.cri.fraud.api.domain.audit.Evidence;
import uk.gov.di.ipv.cri.fraud.api.domain.audit.EvidenceType;
import uk.gov.di.ipv.cri.fraud.api.domain.audit.VCISSFraudAuditExtension;
import uk.gov.di.ipv.cri.fraud.api.persistence.item.FraudResultItem;

import java.util.ArrayList;
import java.util.List;

public class IssueCredentialFraudAuditExtensionUtil {

    private IssueCredentialFraudAuditExtensionUtil() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static VCISSFraudAuditExtension generateVCISSFraudAuditExtension(
            String vcIssuer, List<FraudResultItem> fraudResultItems) {

        List<Evidence> evidenceList = new ArrayList<>();

        for (FraudResultItem fraudResultItem : fraudResultItems) {

            Evidence evidence = new Evidence();

            evidence.setType(EvidenceType.IDENTITY_CHECK.toString());
            evidence.setTxn(fraudResultItem.getTransactionId());
            evidence.setIdentityFraudScore(fraudResultItem.getIdentityFraudScore());
            evidence.setCi(fraudResultItem.getContraIndicators());
            evidence.setDecisionScore(fraudResultItem.getDecisionScore());

            evidenceList.add(evidence);
        }

        return new VCISSFraudAuditExtension(vcIssuer, evidenceList);
    }
}
