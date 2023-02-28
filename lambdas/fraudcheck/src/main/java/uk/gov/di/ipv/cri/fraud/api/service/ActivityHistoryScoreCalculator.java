package uk.gov.di.ipv.cri.fraud.api.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ActivityHistoryScoreCalculator {

    public ActivityHistoryScoreCalculator() {}

    public int calculateActivityHistoryScore(String oldestDate) {
        if (oldestDate != null) {
            LocalDate date = LocalDate.parse(oldestDate, DateTimeFormatter.ofPattern("yyddMM"));
            Duration diff = Duration.between(LocalDate.now().atStartOfDay(), date.atStartOfDay());
            long diffDays = diff.toDays();
            long dateInYears = diffDays / 365;
            if (dateInYears > 0.5 && dateInYears < 2) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
