package uk.gov.di.ipv.cri.fraud.library.util;

import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.library.HttpResponseFixtures;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class HTTPReplyHelperTest {

    private final String ENDPOINT_NAME = "Test Endpoint";
    private final String NO_BODY_TEXT_FORMAT = "No %s response body text found";

    @Test
    void shouldRetrieveResult() throws OAuthErrorResponseException {

        int expectedStatusCode = 200;
        String expectedBodyContent = "Test Response Body";
        String expectedTestHeaderKey = "X-TEST-HEADER";
        String expectedTestHeaderValue = "test_value";

        Map<String, String> expectedHeadersMap = new HashMap<>();
        expectedHeadersMap.put(expectedTestHeaderKey, expectedTestHeaderValue);

        HttpResponse mockResponse =
                HttpResponseFixtures.createHttpResponse(
                        expectedStatusCode, expectedHeadersMap, expectedBodyContent, false);

        HTTPReply reply = HTTPReplyHelper.retrieveResponse(mockResponse, ENDPOINT_NAME);

        assertEquals(expectedBodyContent, reply.responseBody);
        assertEquals(expectedStatusCode, reply.statusCode);
        assertNotNull(reply.responseHeaders.get(expectedTestHeaderKey));
        assertEquals(expectedTestHeaderValue, reply.responseHeaders.get(expectedTestHeaderKey));
    }

    @Test
    void shouldSetNoBodyTextWhenEntityUtilsReturnsNull() throws OAuthErrorResponseException {

        int expectedStatusCode = 200;
        String expectedBodyContent = String.format(NO_BODY_TEXT_FORMAT, ENDPOINT_NAME);

        HttpResponse mockResponse =
                HttpResponseFixtures.createHttpResponse(expectedStatusCode, null, null, false);

        HTTPReply reply = HTTPReplyHelper.retrieveResponse(mockResponse, ENDPOINT_NAME);

        assertEquals(expectedBodyContent, reply.responseBody);
        assertEquals(expectedStatusCode, reply.statusCode);
    }

    @Test
    void shouldThrowOAuthHttpResponseExceptionWhenIOExceptionEncounteredRetrievingHTTPReply() {

        HttpResponse mockResponse = HttpResponseFixtures.createHttpResponse(200, null, null, true);

        OAuthErrorResponseException thrownException =
                assertThrows(
                        OAuthErrorResponseException.class,
                        () -> HTTPReplyHelper.retrieveResponse(mockResponse, ENDPOINT_NAME));

        assertEquals("Failed to retrieve http response body", thrownException.getErrorReason());
    }
}
