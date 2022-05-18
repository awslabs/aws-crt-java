/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.http.HttpVersion;

/**
 * Represents a single Client Request to be sent on a HTTP connection
 */
public class Http2Request extends HttpRequestBase {
    /**
     * An empty HTTP/2 Request.
     */
    public Http2Request() {
        this(new HttpHeader[] {}, null);
    }

    /**
     * An empty HTTP/2 Request with headers and body stream.
     *
     * @param headers    set of http request headers to include, note: pseudo
     *                   headers should be set to make a good HTTP/2 request.
     * @param bodyStream (optional) interface to an object that will stream out the
     *                   request body
     */
    public Http2Request(HttpHeader[] headers, HttpRequestBodyStream bodyStream) {
        super(headers, bodyStream);
        this.version = HttpVersion.HTTP_2;
    }

}
