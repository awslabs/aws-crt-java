/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single Client Request to be sent on a HTTP connection
 */
public class HttpRequest {
    private final static Charset UTF8 = java.nio.charset.StandardCharsets.UTF_8;
    private final static int BUFFER_INT_SIZE = 4;
    private final String method;
    private String encodedPath;
    private List<HttpHeader> headers;
    private HttpRequestBodyStream bodyStream;

    /**
     *
     * @param method http verb to use
     * @param encodedPath path of the http request
     */
    public HttpRequest(String method, String encodedPath) {
        this(method, encodedPath, new HttpHeader[]{}, null);
    }

    /**
     *
     * @param method http verb to use
     * @param encodedPath path of the http request
     * @param headers set of http request headers to include
     * @param bodyStream (optional) interface to an object that will stream out the request body
     */
    public HttpRequest(String method, String encodedPath, HttpHeader[] headers, HttpRequestBodyStream bodyStream) {
        if (headers == null) { throw new IllegalArgumentException("Headers can be empty, but can't be null"); }
        this.method = method;
        this.encodedPath = encodedPath;
        this.headers = Arrays.asList(headers);
        this.bodyStream = bodyStream;
    }

    /**
     * Package private. Used by JNI to convert native http representation to a Java object, because accessing this
     * struct from JNI is too slow.
     *
     *  Requests are marshalled as follows:
     *
     *  each string field is:
     *  [4-bytes BE] [variable length bytes specified by the previous field]
     *  Each request is then:
     *  [method][path][header name-value pairs]
     *
     * @param marshalledRequest serialized http request to be parsed.
     * @param bodyStream body stream for the http request.
     */
    HttpRequest(ByteBuffer marshalledRequest, HttpRequestBodyStream bodyStream) {
        if (marshalledRequest.remaining() < BUFFER_INT_SIZE * 2) {
            throw new CrtRuntimeException("Invalid marshalled request object.");
        }

        int methodLength = marshalledRequest.getInt();
        byte[] methodBlob = new byte[methodLength];
        marshalledRequest.get(methodBlob);
        this.method = new String(methodBlob, UTF8);
        if  (marshalledRequest.remaining() < BUFFER_INT_SIZE) {
            throw new CrtRuntimeException("Invalid marshalled request object.");
        }

        int pathLength = marshalledRequest.getInt();
        byte[] pathBlob = new byte[pathLength];
        marshalledRequest.get(pathBlob);
        this.encodedPath = new String(pathBlob, UTF8);

        this.headers = HttpHeader.loadHeadersListFromMarshalledHeadersBlob(marshalledRequest);
        this.bodyStream = bodyStream;
    }

    public String getMethod() {
        return method;
    }

    public String getEncodedPath() {
        return encodedPath;
    }

    public void setEncodedPath(final String encodedPath) {
        this.encodedPath = encodedPath;
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

    public HttpRequestBodyStream getBodyStream() {
        return bodyStream;
    }

    /**
     * Requests are marshalled as follows:
     *
     * each string field is:
     * [4-bytes BE] [variable length bytes specified by the previous field]
     *
     * Each request is then:
     * [method][path][header name-value pairs]
     * @return encoded blob of headers
     */
    public byte[] marshalForJni() {
        int size = 0;
        size += BUFFER_INT_SIZE + method.length();
        size += BUFFER_INT_SIZE + encodedPath.length();
        size += (BUFFER_INT_SIZE * 2) * headers.size();

        for(HttpHeader header : headers) {
            if (header.getNameBytes().length > 0) {
                size += header.getNameBytes().length + header.getValueBytes().length;
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(method.length());
        buffer.put(method.getBytes(UTF8));
        buffer.putInt(encodedPath.length());
        buffer.put(encodedPath.getBytes(UTF8));

        for(HttpHeader header : headers) {
            if (header.getNameBytes().length > 0) {
                buffer.putInt(header.getNameBytes().length);
                buffer.put(header.getNameBytes());
                buffer.putInt(header.getValueBytes().length);
                buffer.put(header.getValueBytes());
            }
        }

        return buffer.array();
    }
}
