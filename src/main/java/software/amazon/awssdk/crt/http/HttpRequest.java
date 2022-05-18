/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpVersion;

import java.nio.ByteBuffer;

/**
 * Represents a single Client Request to be sent on a HTTP connection
 */
public class HttpRequest extends HttpRequestBase {

    /**
     *
     * @param method      http verb to use
     * @param encodedPath path of the http request
     */
    public HttpRequest(String method, String encodedPath) {
        this(method, encodedPath, new HttpHeader[] {}, null);
    }

    /**
     *
     * @param method      http verb to use
     * @param encodedPath path of the http request
     * @param headers     set of http request headers to include
     * @param bodyStream  (optional) interface to an object that will stream out the
     *                    request body
     */
    public HttpRequest(String method, String encodedPath, HttpHeader[] headers, HttpRequestBodyStream bodyStream) {
        super(headers, bodyStream);
        this.method = method;
        this.encodedPath = encodedPath;
    }

    /**
     * Package private. Used by JNI to convert native http representation to a Java
     * object, because accessing this struct from JNI is too slow.
     *
     * Requests are marshalled as follows:
     *
     * each string field is: [4-bytes BE] [variable length bytes specified by the
     * previous field] Each request is then: [method][path][header name-value pairs]
     *
     * @param marshalledRequest serialized http request to be parsed.
     * @param bodyStream        body stream for the http request.
     */
    HttpRequest(ByteBuffer marshalledRequest, HttpRequestBodyStream bodyStream) {
        if (marshalledRequest.remaining() < BUFFER_INT_SIZE * 3) {
            throw new CrtRuntimeException("Invalid marshalled request object.");
        }
        this.version = HttpVersion.getEnumValueFromInteger(marshalledRequest.getInt());

        int methodLength = marshalledRequest.getInt();
        byte[] methodBlob = new byte[methodLength];
        marshalledRequest.get(methodBlob);
        this.method = new String(methodBlob, UTF8);
        if (marshalledRequest.remaining() < BUFFER_INT_SIZE) {
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
     * 
     * @param encodedPath the new encoded path
     */
    public void setEncodedPath(final String encodedPath) {
        this.encodedPath = encodedPath;
    }
}
