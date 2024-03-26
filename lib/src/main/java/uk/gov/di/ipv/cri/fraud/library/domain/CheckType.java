package uk.gov.di.ipv.cri.fraud.library.domain;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public enum CheckType {
    // Used to record checks in FraudCheck.
    // Passed though to Issuecredential and converted to lowercase strings for VC evidence
    MORTALITY_CHECK,
    IDENTITY_THEFT_CHECK,
    SYNTHETIC_IDENTITY_CHECK,
    IMPERSONATION_RISK_CHECK,
    ACTIVITY_HISTORY_CHECK // Note check name not display
}
