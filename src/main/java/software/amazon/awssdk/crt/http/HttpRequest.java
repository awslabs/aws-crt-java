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

/**
 * Represents a single Client Request to be sent on a HTTP connection
 */
public class HttpRequest {
    private final String method;
    private final String encodedPath;
    private final HttpHeader[] headers;

    public HttpRequest(String method, String encodedPath) {
        this(method, encodedPath, new HttpHeader[]{});
    }

    public HttpRequest(String method, String encodedPath, HttpHeader[] headers) {
        if (headers == null) { throw new IllegalArgumentException("Headers can be empty, but can't be null"); }
        this.method = method;
        this.encodedPath = encodedPath;
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public String getEncodedPath() {
        return encodedPath;
    }

    public HttpHeader[] getHeaders() {
        return headers;
    }
}
