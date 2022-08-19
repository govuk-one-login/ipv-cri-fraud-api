package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IdentityScoreCalculator {

    private static final Logger LOGGER = LogManager.getLogger();

    public int calculateIdentityScore(
            boolean thirdPartyFraudCheckSuccess, boolean thirdPartyPepCheckSuccess) {
        if (thirdPartyFraudCheckSuccess && thirdPartyPepCheckSuccess) {
            return 2;
        } else if (thirdPartyFraudCheckSuccess && !thirdPartyPepCheckSuccess) {
            return 1;
        } else {
            return 0;
        }
    }
}
