package uk.gov.di.ipv.cri.fraud.library.domain;

public enum CheckType {
    // Used to record checks in FraudCheck.
    // Passed though to Issuecredential and converted to lowercase strings for VC evidence
    MORTALITY_CHECK,
    IDENTITY_THEFT_CHECK,
    SYNTHETIC_IDENTITY_CHECK,
    IMPERSONATION_RISK_CHECK,
}
