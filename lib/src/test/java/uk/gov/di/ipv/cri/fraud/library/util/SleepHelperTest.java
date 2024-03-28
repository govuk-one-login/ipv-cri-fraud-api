package uk.gov.di.ipv.cri.fraud.library.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SleepHelperTest {

    private SleepHelper sleepHelper;

    private final long MAX_TEST_SLEEP = 6400L;

    // A margin to account for processing speed/delays
    // Waits can be intermittently slower depending
    // on what is happening when tests are running
    private final long EPSILON = 20L;

    @BeforeEach
    void setUp() {
        sleepHelper = new SleepHelper(MAX_TEST_SLEEP);
    }

    @Test
    void shouldBusyWait0msWhenCalledOnce() {
        long expectedWait = 0;

        long waitTime = sleepHelper.busyWaitWithExponentialBackOff(0);

        assertEquals(expectedWait, waitTime, EPSILON);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7})
    void shouldBusyWait2P100ForCallN(int callNumber) {
        long baseWaitTime = 100;
        long power = callNumber - 1;

        long expectedWait = (long) Math.pow(2, power) * baseWaitTime;

        long waitTime = sleepHelper.busyWaitWithExponentialBackOff(callNumber);

        assertEquals(expectedWait, waitTime, EPSILON);
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 9})
    void shouldBusyWaitMaxTimeWhenCalledMoreThan7Times(int callNumber) {
        long expectedWait = MAX_TEST_SLEEP;

        long waitTime = sleepHelper.busyWaitWithExponentialBackOff(callNumber);

        assertEquals(expectedWait, waitTime, EPSILON);
    }

    @ParameterizedTest
    @ValueSource(ints = {250, 500, 750, 1000})
    void shouldBusyWaitNMilliseconds(int expectedWaitTime) {

        long waitTime = sleepHelper.busyWaitMilliseconds(expectedWaitTime);

        assertEquals(expectedWaitTime, waitTime, EPSILON);
    }
}
