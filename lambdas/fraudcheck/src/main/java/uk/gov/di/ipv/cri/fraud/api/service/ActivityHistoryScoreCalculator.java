package uk.gov.di.ipv.cri.fraud.api.service;

public class ActivityHistoryScoreCalculator {

    public ActivityHistoryScoreCalculator() {
        /* Intended */
    }

    public int calculateActivityHistoryScore(Integer monthsBetween) {
        if (null != monthsBetween) {
            // add additional Activity History score scenarios here once score is updated to 2+
            if (monthsBetween >= 6) {
                return 1;
            } else {
                // 6 months or less = score 0
                return 0;
            }
        } else {
            // No value found = score 0
            return 0;
        }
    }
}
