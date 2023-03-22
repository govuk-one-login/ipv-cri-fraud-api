package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ActivityHistoryScoreCalculatorTest {
    private ActivityHistoryScoreCalculator activityHistoryScoreCalculator;

    @BeforeEach
    void setup() {
        activityHistoryScoreCalculator = new ActivityHistoryScoreCalculator();
    }

    @Test
    void testActivityHistoryScore0() {
        YearMonth date3MonthsAgoAsYyyMM = YearMonth.now().minusMonths(3);

        Integer monthsBetween =
                Math.toIntExact(ChronoUnit.MONTHS.between(date3MonthsAgoAsYyyMM, YearMonth.now()));

        int activityHistoryScore =
                activityHistoryScoreCalculator.calculateActivityHistoryScore(monthsBetween);
        assertEquals(0, activityHistoryScore);
    }

    @Test
    void testActivityHistoryScore1() {
        YearMonth date1YearAgoAsYyyMM = YearMonth.now().minusMonths(12);
        Integer monthsBetween =
                Math.toIntExact(ChronoUnit.MONTHS.between(date1YearAgoAsYyyMM, YearMonth.now()));

        int activityHistoryScore =
                activityHistoryScoreCalculator.calculateActivityHistoryScore(monthsBetween);
        assertEquals(1, activityHistoryScore);
    }

    @Test
    void testActivityHistoryScoreNull() {
        int activityHistoryScore =
                activityHistoryScoreCalculator.calculateActivityHistoryScore(null);
        assertEquals(0, activityHistoryScore);
    }
}
