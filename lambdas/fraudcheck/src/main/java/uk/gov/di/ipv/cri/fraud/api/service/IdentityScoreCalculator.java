package uk.gov.di.ipv.cri.fraud.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.fraud.api.domain.check.FraudCheckResult;

import java.util.Arrays;
import java.util.List;

public class IdentityScoreCalculator {

    private static final Logger LOGGER = LogManager.getLogger();
    private List<String> zeroScoreUcodes;
    private Integer noFileFoundThreshold = 0;

    public IdentityScoreCalculator(ConfigurationService configurationService) {
        zeroScoreUcodes = configurationService.getZeroScoreUcodes();
        noFileFoundThreshold = configurationService.getNoFileFoundThreshold();
    }

    public int calculateIdentityScoreAfterFraudCheck(
            FraudCheckResult fraudCheckResult, boolean checkSuccess) {

        if (checkSuccess) {
            Integer decisionScore = Integer.valueOf(fraudCheckResult.getDecisionScore());
            for (String zeroScoreUcode : zeroScoreUcodes) {
                if (Arrays.asList(fraudCheckResult.getThirdPartyFraudCodes())
                        .contains(zeroScoreUcode)) {
                    LOGGER.info(
                            "User has been identified as having a ucode that indicates they cannnot score higher than zero");
                    return 0;
                }
            }

            if (decisionScore <= noFileFoundThreshold) {
                LOGGER.info(
                        "Decision score was below the file found threshold they cannot score higher than one");
                return 1;
            }

            return 1;
        }

        return 0;
    }

    public int calculateIdentityScoreAfterPEPCheck(int currentScore, boolean checkSuccess) {

        // Required score for PEP check to have been performed is 1
        if (currentScore == 1 && checkSuccess) {
            return 2;
        } else if (currentScore != 1) {
            LOGGER.error(
                    "PEP should not have been performed with current score of {}", currentScore);
        }

        // no change to score
        return currentScore;
    }
}
