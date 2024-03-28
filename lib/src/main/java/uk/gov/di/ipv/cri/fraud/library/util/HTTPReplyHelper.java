package uk.gov.di.ipv.cri.fraud.library.util;

import lombok.experimental.UtilityClass;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class HTTPReplyHelper {

    private static final Logger LOGGER = LogManager.getLogger();

    // Small helper to avoid duplicating this code for each endpoint and api
    public static HTTPReply retrieveResponse(HttpResponse response, String endpointName)
            throws OAuthErrorResponseException {
        try {
            Map<String, String> responseHeaders = getAllHeaders(response);

            String mappedBody = EntityUtils.toString(response.getEntity());

            // EntityUtils can return null
            String responseBody =
                    (mappedBody) == null
                            ? String.format("No %s response body text found", endpointName)
                            : mappedBody;
            int httpStatusCode = response.getStatusLine().getStatusCode();

            return new HTTPReply(httpStatusCode, responseHeaders, responseBody);
        } catch (IOException e) {

            LOGGER.error(String.format("IOException retrieving %s response body", endpointName));
            LOGGER.debug(e.getMessage());

            throw new OAuthErrorResponseException(
                    HttpStatusCode.INTERNAL_SERVER_ERROR,
                    ErrorResponse.FAILED_TO_RETRIEVE_HTTP_RESPONSE_BODY);
        }
    }

    private static Map<String, String> getAllHeaders(HttpResponse response) {

        Map<String, String> headers = new HashMap<>();

        Header[] apacheHeaders = response.getAllHeaders();

        for (Header apacheHeader : apacheHeaders) {
            headers.put(apacheHeader.getName(), apacheHeader.getValue());
        }

        return headers;
    }
}
