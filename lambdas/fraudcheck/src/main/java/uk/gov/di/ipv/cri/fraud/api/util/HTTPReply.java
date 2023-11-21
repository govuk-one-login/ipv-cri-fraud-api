package uk.gov.di.ipv.cri.fraud.api.util;

import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;

import java.util.Map;

public class HTTPReply {
    public final int statusCode;
    public final Map<String, String> responseHeaders;
    public final String responseBody;

    public HTTPReply(int statusCode, Map<String, String> responseHeaders, String responseBody) {
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    @ExcludeFromGeneratedCoverageReport
    private HTTPReply() {
        statusCode = -1;
        responseHeaders = null;
        responseBody = null;

        throw new IllegalStateException("Not Valid to call no args constructor for this class");
    }
}
