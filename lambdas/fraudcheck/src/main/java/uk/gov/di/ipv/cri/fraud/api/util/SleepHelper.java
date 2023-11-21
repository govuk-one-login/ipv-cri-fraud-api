package uk.gov.di.ipv.cri.fraud.api.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SleepHelper {

    private static final Logger LOGGER = LogManager.getLogger();

    public final long maxSleepTimeMs;

    public SleepHelper(long maxSleepTimeMs) {
        this.maxSleepTimeMs = maxSleepTimeMs;
    }

    /**
     * Calculates a wait time based on number of calls - starting from zero for the first call.
     * Using a busy wait
     *
     * @param callNumber
     * @return
     */
    public long busyWaitWithExponentialBackOff(int callNumber) {

        long waitDuration = Math.min(calculateExponentialBackOffTimeMS(callNumber), maxSleepTimeMs);

        long startTime = System.currentTimeMillis();
        long futureTime = startTime + waitDuration;

        LOGGER.info("busyWaitWithExponentialBackOff start time : {}", startTime);

        while (System.currentTimeMillis() < futureTime) {
            // Intended
        }

        long endTime = System.currentTimeMillis();
        long timeWaited = (endTime - startTime);

        LOGGER.info("busyWaitWithExponentialBackOff end time : {}", endTime);

        return timeWaited;
    }

    private long calculateExponentialBackOffTimeMS(int callNumber) {

        if (callNumber == 0) {
            return 0;
        }

        int power = callNumber - 1;

        return ((long) Math.pow(2, power) * 100L);
    }
}
