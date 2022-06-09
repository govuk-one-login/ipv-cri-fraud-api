package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IdentityScoreCalaculator {

    private static final Logger LOGGER = LogManager.getLogger();

    public IdentityScoreCalaculator() {}

    public int calculateIdentityScore(
            boolean thirdPartyFraudCheckSuccess, String[] contraIndicators) {
        if (thirdPartyFraudCheckSuccess) {
            return 1;
        } else {
            return 0;
        }
    }
}
