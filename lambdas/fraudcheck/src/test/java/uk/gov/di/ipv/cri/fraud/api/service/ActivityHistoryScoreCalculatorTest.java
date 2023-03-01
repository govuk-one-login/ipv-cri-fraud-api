package uk.gov.di.ipv.cri.fraud.api.service;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.IdentityVerificationResponseMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityHistoryScoreCalculatorTest {
    private ActivityHistoryScoreCalculator activityHistoryScoreCalculator;

    @BeforeEach
    void setup()
    {
        activityHistoryScoreCalculator = new ActivityHistoryScoreCalculator();
    }

    @Test
    void testActivityHistoryScore0() {
        String oldestDate = DateTime.now().minusMonths(3).toString("yyddMM");
        int activityHistoryScore = activityHistoryScoreCalculator.calculateActivityHistoryScore(oldestDate);
        assertEquals(0, activityHistoryScore);
    }

    @Test
    void testActivityHistoryScore1() {
        String oldestDate = DateTime.now().minusYears(1).toString("yyddMM");
        int activityHistoryScore = activityHistoryScoreCalculator.calculateActivityHistoryScore(oldestDate);
        assertEquals(1, activityHistoryScore);
    }
}
