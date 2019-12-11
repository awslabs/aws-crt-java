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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a single Client Request to be sent on a HTTP connection
 */
public class HttpRequest {
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

    public List<HttpHeader> getHeaders() {
        return headers;
    }

    public void addHeader(final HttpHeader header) {
        headers.add(header);
    }

    public void addHeader(final String headerName, final String headerValue) {
        headers.add(new HttpHeader(headerName, headerValue));
    }

    public void addHeaders(final HttpHeader headers) {
        this.headers.add(headers);
    }

    public void addQueryParam(final String paramName, final String paramValue, boolean urlEncode) {
        String prefix = "?";
        if (encodedPath != null) {
            if (!encodedPath.contains("?")) {
                prefix = "&";
            }
        } else {
            encodedPath = "";
        }

        encodedPath += prefix;
        try {
            if (urlEncode) {
                encodedPath += URLEncoder.encode(paramName, "UTF-8");
            } else {
                encodedPath += paramName;
            }

            encodedPath += "=";

            if (urlEncode) {
                encodedPath += URLEncoder.encode(paramValue, "UTF-8");
            } else {
                encodedPath += paramValue;
            }
        } catch (UnsupportedEncodingException e) {
            // yeah, there's nothing to be done about this one.
            Runtime.getRuntime().halt(-1);
        }
    }

    public HttpRequestBodyStream getBodyStream() {
        return bodyStream;
    }
}
