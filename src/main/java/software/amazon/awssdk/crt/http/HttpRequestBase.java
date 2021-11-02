package software.amazon.awssdk.crt.http;

import java.util.Arrays;
import java.util.List;

import software.amazon.awssdk.crt.http.HttpClientConnection.ProtocolVersion;
import java.util.Arrays;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.util.Collections;

public class HttpRequestBase {
    protected final static Charset UTF8 = java.nio.charset.StandardCharsets.UTF_8;
    protected final static int BUFFER_INT_SIZE = 4;
    protected List<HttpHeader> headers;
    protected HttpRequestBodyStream bodyStream;
    protected ProtocolVersion version = ProtocolVersion.HTTP_1_1;
    protected String method;
    protected String encodedPath;

    protected HttpRequestBase() {
    }

    protected HttpRequestBase(HttpHeader[] headers, HttpRequestBodyStream bodyStream) {
        if (headers == null) {
            throw new IllegalArgumentException("Headers can be empty, but can't be null");
        }
        this.headers = Arrays.asList(headers);
        this.bodyStream = bodyStream;
    }

    /**
     * @exclude Requests are marshalled as follows:
     *
     *          version is as int: [4-bytes BE]
     *
     *          each string field is: [4-bytes BE] [variable length bytes specified
     *          by the previous field]
     *
     *          Each request is then: [version][method][path][header name-value
     *          pairs]
     * @return encoded blob of headers
     */
    public byte[] marshalForJni() {
        int size = 0;
        size += BUFFER_INT_SIZE; /* version */
        size += BUFFER_INT_SIZE + method.length();
        size += BUFFER_INT_SIZE + encodedPath.length();
        size += (BUFFER_INT_SIZE * 2) * headers.size();

        for (HttpHeader header : headers) {
            if (header.getNameBytes().length > 0) {
                size += header.getNameBytes().length + header.getValueBytes().length;
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(version.getValue());
        buffer.putInt(method.length());
        buffer.put(method.getBytes(UTF8));
        buffer.putInt(encodedPath.length());
        buffer.put(encodedPath.getBytes(UTF8));

        for (HttpHeader header : headers) {
            if (header.getNameBytes().length > 0) {
                buffer.putInt(header.getNameBytes().length);
                buffer.put(header.getNameBytes());
                buffer.putInt(header.getValueBytes().length);
                buffer.put(header.getValueBytes());
            }
        }

        return buffer.array();
    }

    public HttpRequestBodyStream getBodyStream() {
        return bodyStream;
    }

    public List<HttpHeader> getHeaders() {
        return headers;
    }

    public HttpHeader[] getHeadersAsArray() {
        return headers.toArray(new HttpHeader[] {});
    }

    public void addHeader(final HttpHeader header) {
        headers.add(header);
    }

    public void addHeader(final String headerName, final String headerValue) {
        headers.add(new HttpHeader(headerName, headerValue));
    }

    public void addHeaders(final HttpHeader[] headers) {
        Collections.addAll(this.headers, headers);

    }
}
