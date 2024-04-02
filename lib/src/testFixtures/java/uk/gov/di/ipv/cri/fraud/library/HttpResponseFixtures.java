package uk.gov.di.ipv.cri.fraud.library;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HttpResponseFixtures {
    private HttpResponseFixtures() {
        throw new IllegalStateException("Test Fixtures");
    }

    // Used to create response scenarios in unit tests
    public static CloseableHttpResponse createHttpResponse(
            int statusCode,
            Map<String, String> headerMap,
            String responseBody,
            boolean ioException) {

        final Header[] apacheHeaders = createHeaders(headerMap);

        return new CloseableHttpResponse() {

            @Override
            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            @Override
            public boolean containsHeader(String name) {
                return false;
            }

            @Override
            public Header[] getHeaders(String name) {
                return new Header[0];
            }

            @Override
            public Header getFirstHeader(String name) {
                return null;
            }

            @Override
            public Header getLastHeader(String name) {
                return null;
            }

            @Override
            public Header[] getAllHeaders() {
                return apacheHeaders;
            }

            @Override
            public void addHeader(Header header) {
                // Test Fixture
            }

            @Override
            public void addHeader(String name, String value) {
                // Test Fixture
            }

            @Override
            public void setHeader(Header header) {
                // Test Fixture
            }

            @Override
            public void setHeader(String name, String value) {
                // Test Fixture
            }

            @Override
            public void setHeaders(Header[] headers) {
                // Test Fixture
            }

            @Override
            public void removeHeader(Header header) {
                // Test Fixture
            }

            @Override
            public void removeHeaders(String name) {
                // Test Fixture
            }

            @Override
            public HeaderIterator headerIterator() {
                return new HeaderIterator() {
                    int i = 0;

                    @Override
                    public boolean hasNext() {
                        return i < apacheHeaders.length;
                    }

                    @Override
                    public Header nextHeader() {
                        return apacheHeaders[i++];
                    }

                    @Override
                    public Object next() {
                        return nextHeader();
                    }

                    @Override
                    public void remove() {
                        //
                    }
                };
            }

            @Override
            public HeaderIterator headerIterator(String name) {
                return headerIterator();
            }

            @Override
            public HttpParams getParams() {
                return null;
            }

            @Override
            public void setParams(HttpParams params) {
                // Test Fixture
            }

            @Override
            public StatusLine getStatusLine() {
                return new StatusLine() {
                    @Override
                    public ProtocolVersion getProtocolVersion() {
                        return null;
                    }

                    @Override
                    public int getStatusCode() {
                        return statusCode;
                    }

                    @Override
                    public String getReasonPhrase() {
                        return "OK";
                    }
                };
            }

            @Override
            public void setStatusLine(StatusLine statusline) {
                // Test Fixture
            }

            @Override
            public void setStatusLine(ProtocolVersion ver, int code) {
                // Test Fixture
            }

            @Override
            public void setStatusLine(ProtocolVersion ver, int code, String reason) {
                // Test Fixture
            }

            @Override
            public void setStatusCode(int code) throws IllegalStateException {
                // Test Fixture
            }

            @Override
            public void setReasonPhrase(String reason) throws IllegalStateException {
                // Test Fixture
            }

            @Override
            public HttpEntity getEntity() {

                ByteArrayInputStream bai = null;
                long contentLength = 0;
                if (responseBody != null) {
                    contentLength = responseBody.getBytes().length;
                    bai = new ByteArrayInputStream(responseBody.getBytes());
                }

                final long finalContentLength = contentLength;
                final ByteArrayInputStream finalBai = bai;

                return new HttpEntity() {
                    @Override
                    public boolean isRepeatable() {
                        return false;
                    }

                    @Override
                    public boolean isChunked() {
                        return false;
                    }

                    @Override
                    public long getContentLength() {
                        return finalContentLength;
                    }

                    @Override
                    public Header getContentType() {
                        return null;
                    }

                    @Override
                    public Header getContentEncoding() {
                        return null;
                    }

                    @Override
                    public InputStream getContent() throws IOException {

                        if (!ioException) {
                            return finalBai;
                        }

                        throw new IOException("ERROR!");
                    }

                    @Override
                    public void writeTo(OutputStream outStream) throws IOException {
                        // Test Fixture
                    }

                    @Override
                    public boolean isStreaming() {
                        return false;
                    }

                    @Override
                    public void consumeContent() throws IOException {
                        // Test Fixture
                    }
                };
            }

            @Override
            public void setEntity(HttpEntity entity) {
                // Test Fixture
            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public void setLocale(Locale loc) {
                // Test Fixture
            }

            @Override
            public void close() throws IOException {
                // Test Fixture
            }
        };
    }

    private static Header[] createHeaders(Map<String, String> headerMap) {

        if (null == headerMap) {
            return new Header[0];
        }

        int numHeaders = headerMap.size();

        Header[] headers = new Header[numHeaders];

        List<String> keys = new ArrayList<>(headerMap.keySet());

        for (int h = 0; h < numHeaders; h++) {

            String key = keys.get(0);
            String value = headerMap.get(key);

            headers[h] = new BasicHeader(key, value);
        }

        return headers;
    }
}
