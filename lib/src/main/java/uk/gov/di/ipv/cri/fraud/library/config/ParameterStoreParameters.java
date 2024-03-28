package uk.gov.di.ipv.cri.fraud.library.config;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

public class ParameterStoreParameters {

    // **************************** CRI ****************************

    public static final String FRAUD_RESULT_ITEM_TABLE_NAME = "FraudTableName";
    public static final String FRAUD_RESULT_ITEM_TTL_PARAMETER =
            "SessionTtl"; // Linked to Common SessionTTL

    // ************************ Issue Cred VC ************************

    public static final String MAX_JWT_TTL_UNIT = "JwtTtlUnit"; // Issue Cred VC TTL

    @ExcludeFromGeneratedCoverageReport
    private ParameterStoreParameters() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }
}
