/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.crt.http;
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

    public byte[] marshallForJni() {
        int size = 0;
        size += 4+ method.length();
        size += 4 + encodedPath.length();

        for(HttpHeader header : headers) {
            size += 8 + header.getNameBytes().length + header.getValueBytes().length;
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(method.length());
        buffer.put(method.getBytes(UTF8));
        buffer.putInt(encodedPath.length());
        buffer.put(encodedPath.getBytes(UTF8));

        for(HttpHeader header : headers) {
            buffer.putInt(header.getNameBytes().length);
            buffer.put(header.getNameBytes());
            buffer.putInt(header.getValueBytes().length);
            buffer.put(header.getValueBytes());
        }

        return buffer.array();
    }
}
