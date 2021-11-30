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

    /**
     * @return the HTTP method of this request
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return the encoded path of this request
     */
    public String getEncodedPath() {
        return encodedPath;
    }

    /**
     * Sets the request's encoded path
     * @param encodedPath the new encoded path
     */
    public void setEncodedPath(final String encodedPath) {
        this.encodedPath = encodedPath;
    }

    /**
     * @return A list of the headers for this request
     */
    public List<HttpHeader> getHeaders() {
        return headers;
    }

    /**
     * @return An array of the headers for this request
     */
    public HttpHeader[] getHeadersAsArray() {
        return headers.toArray(new HttpHeader[] {});
    }

    /**
     * Adds a header to this request's header set
     * @param header a new header to add to the header set
     */
    public void addHeader(final HttpHeader header) {
        headers.add(header);
    }

    /**
     * Adds a header to this request's header set
     * @param headerName name of the header to add
     * @param headerValue value of the header to add
     */
    public void addHeader(final String headerName, final String headerValue) {
        headers.add(new HttpHeader(headerName, headerValue));
    }

    /**
     * Add multiple headers to this request's header set
     * @param headers array of headers to add
     */
    public void addHeaders(final HttpHeader[] headers) {
        Collections.addAll(this.headers, headers);

    }

    /**
     * @return the body stream associated with this request
     */
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

        byte[] pathBytes = encodedPath.getBytes(UTF8);
        size += BUFFER_INT_SIZE + pathBytes.length;
    
        for (HttpHeader header : headers) {
            if (header.getNameBytes().length > 0) {
                size += header.getNameBytes().length + header.getValueBytes().length + (BUFFER_INT_SIZE * 2);
            }
        }
    
        ByteBuffer buffer = ByteBuffer.allocate(size);
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
}
