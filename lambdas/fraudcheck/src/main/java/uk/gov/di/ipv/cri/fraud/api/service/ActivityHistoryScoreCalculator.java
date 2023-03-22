package uk.gov.di.ipv.cri.fraud.api.service;

public class ActivityHistoryScoreCalculator {

    public ActivityHistoryScoreCalculator() {}

    public int calculateActivityHistoryScore(Integer monthsBetween) {
        if (null != monthsBetween) {
            // add additional Activity History score scenarios here once score is updated to 2+
            if (monthsBetween >= 6) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
