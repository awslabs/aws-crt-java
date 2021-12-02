package software.amazon.awssdk.crt.http;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.crt.http.HttpVersion;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.util.Collections;

public class HttpRequestBase {
    protected final static Charset UTF8 = java.nio.charset.StandardCharsets.UTF_8;
    protected final static int BUFFER_INT_SIZE = 4;
    protected List<HttpHeader> headers;
    protected HttpRequestBodyStream bodyStream;
    protected HttpVersion version = HttpVersion.HTTP_1_1;
    protected String method;
    protected String encodedPath;

    /**
     * Only used for create request from native side.
     */
    protected HttpRequestBase() {
    }

    protected HttpRequestBase(HttpHeader[] headers, HttpRequestBodyStream bodyStream) {
        if (headers == null) {
            throw new IllegalArgumentException("Headers can be empty, but can't be null");
        }
        method = "";
        encodedPath = "";
        this.headers = new ArrayList<HttpHeader>(Arrays.asList(headers));
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
        byte[] pathBytes = encodedPath.getBytes(UTF8);
        size += BUFFER_INT_SIZE + pathBytes.length;

        for (HttpHeader header : headers) {
            if (header.getNameBytes().length > 0) {
                size += header.getNameBytes().length + header.getValueBytes().length + (BUFFER_INT_SIZE * 2);
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(version.getValue());
        buffer.putInt(method.length());
        buffer.put(method.getBytes(UTF8));
        buffer.putInt(pathBytes.length);
        buffer.put(pathBytes);

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
