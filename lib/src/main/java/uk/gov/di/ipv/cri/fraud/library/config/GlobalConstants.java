package uk.gov.di.ipv.cri.fraud.library.config;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

public class GlobalConstants {
    public static final String ADDRESS_COUNTRY = "GB";

    @ExcludeFromGeneratedCoverageReport
    private GlobalConstants() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }
}
