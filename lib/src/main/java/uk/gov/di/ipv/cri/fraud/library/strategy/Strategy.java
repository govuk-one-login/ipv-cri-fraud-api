package uk.gov.di.ipv.cri.fraud.library.strategy;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

@ExcludeFromGeneratedCoverageReport
public enum Strategy {
    STUB,
    UAT,
    LIVE,
    NO_CHANGE;

    public static Strategy fromClientIdString(String clientIdString) {
        return switch (clientIdString) {
            case "ipv-core-stub" -> STUB; // Legacy core-stub-id
            case "ipv-core-stub-aws-build" -> STUB;
            case "ipv-core-stub-aws-prod" -> STUB;
            case "ipv-core-stub-aws-build_3rdparty" -> UAT;
            case "ipv-core-stub-aws-prod_3rdparty" -> UAT;
            case "ipv-core-stub-pre-prod-aws-build" -> LIVE;
            case "ipv-core" -> LIVE;
            default -> NO_CHANGE;
        };
    }
}
