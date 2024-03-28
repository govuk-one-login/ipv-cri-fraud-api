package uk.gov.di.ipv.cri.fraud.api.util;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.fraud.api.domain.Evidence;
import uk.gov.di.ipv.cri.fraud.api.domain.audit.VCISSFraudAuditExtension;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

import java.util.ArrayList;
import java.util.List;

public class IssueCredentialFraudAuditExtensionUtil {

    @ExcludeFromGeneratedCoverageReport
    private IssueCredentialFraudAuditExtensionUtil() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static VCISSFraudAuditExtension generateVCISSFraudAuditExtension(
            String vcIssuer, List<FraudResultItem> fraudResultItems) {

        List<Evidence> evidenceList = new ArrayList<>();

        for (FraudResultItem fraudResultItem : fraudResultItems) {

            Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem);

            evidenceList.add(evidence);
        }

        return new VCISSFraudAuditExtension(vcIssuer, evidenceList);
    }
}
