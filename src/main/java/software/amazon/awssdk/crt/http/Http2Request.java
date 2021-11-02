/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.http.HttpClientConnection.ProtocolVersion;

/**
 * Represents a single Client Request to be sent on a HTTP connection
 */
public class Http2Request extends HttpRequestBase {
    /**
     *
     */
    public Http2Request() {
        this(new HttpHeader[] {}, null);
    }

    public Http2Request(HttpHeader[] headers, HttpRequestBodyStream bodyStream) {
        super(headers, bodyStream);
        this.version = ProtocolVersion.HTTP_2;
    }

}
