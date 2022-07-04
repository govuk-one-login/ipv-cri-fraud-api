package uk.gov.di.ipv.cri.fraud.api.util;

public class SleepHelper {
    public final long maxSleepTimeMs;

    public SleepHelper(long maxSleepTimeMs) {
        this.maxSleepTimeMs = maxSleepTimeMs;
    }

    public void sleepWithExponentialBackOff(int tryCount) throws InterruptedException {
        Thread.sleep(Math.min(calculateExponentialBackOffTimeMS(tryCount), maxSleepTimeMs));
    }

    private long calculateExponentialBackOffTimeMS(int tryCount) {
        return ((long) Math.pow(2, tryCount) * 100L);
    }
}
