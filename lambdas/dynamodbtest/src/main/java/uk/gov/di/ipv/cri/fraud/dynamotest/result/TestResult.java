package uk.gov.di.ipv.cri.fraud.dynamotest.result;

public record TestResult(
        long id, String testName, long iterations, long delayMS, long passes, long fails) {}
