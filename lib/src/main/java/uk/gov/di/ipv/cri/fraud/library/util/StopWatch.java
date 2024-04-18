package uk.gov.di.ipv.cri.fraud.library.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StopWatch {

    private long startTime = 0;

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public long stop() {
        return System.currentTimeMillis() - startTime;
    }
}
