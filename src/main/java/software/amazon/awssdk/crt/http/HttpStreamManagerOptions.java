/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.http;


/**
 * Contains all the configuration options for a Http2StreamManager
 * instance
 */
public class HttpStreamManagerOptions {

    private HttpClientConnectionManagerOptions h1ConnectionManagerOptions;
    private Http2StreamManagerOptions h2StreamManagerOptions;
    /**
     * The expected protocol for the stream manager.
     *
     * - UNKNOWN: Default to use HTTP/2, but if server returns an HTTP/1.1 connection back, fallback to the HTTP/1.1 pool
     * - HTTP2: ONLY HTTP/2
     * - HTTP_1_1/HTTP_1_0: ONLY HTTP/1 and HTTP/1.1
     */
    private HttpVersion expectedProtocol;

    /**
     * Default constructor
     */
    public HttpStreamManagerOptions() {
        this.expectedProtocol = HttpVersion.UNKNOWN;
    }

    /**
     * The connection manager options for the HTTP/1.1 stream manager. Controls the behavior for HTTP/1 connections.
     *
     * @param connectionManagerOptions The connection manager options for the underlying HTTP/1.1 stream manager
     * @return this
     */
    public HttpStreamManagerOptions withHTTP1ConnectionManagerOptions(HttpClientConnectionManagerOptions connectionManagerOptions) {
        this.h1ConnectionManagerOptions = connectionManagerOptions;
        return this;
    }

    /**
     * @return The connection manager options for the HTTP/1.1 stream manager.
     */
    public HttpClientConnectionManagerOptions getHTTP1ConnectionManagerOptions() {
        return h1ConnectionManagerOptions;
    }

    /**
     * The stream manager options for the HTTP/2 stream manager. Controls the behavior for HTTP/2 connections.
     *
     * @param streamManagerOptions The stream manager options for the underlying HTTP/2 stream manager
     * @return this
     */
    public HttpStreamManagerOptions withHTTP2StreamManagerOptions(Http2StreamManagerOptions streamManagerOptions) {
        this.h2StreamManagerOptions = streamManagerOptions;
        return this;
    }

    /**
     * @return The stream manager options for the HTTP/2 stream manager.
     */
    public Http2StreamManagerOptions getHTTP2StreamManagerOptions() {
        return h2StreamManagerOptions;
    }

    /**
     * The expected protocol for whole stream manager. Default to UNKNOWN.
     *
     * - UNKNOWN: Default to use HTTP/2, but if server returns an HTTP/1.1 connection back, fallback to the HTTP/1.1 pool
     * - HTTP2: ONLY HTTP/2
     * - HTTP_1_1/HTTP_1_0: ONLY HTTP/1 and HTTP/1.1
     *
     * @param expectedProtocol The stream manager options for the underlying HTTP/2 stream manager
     * @return this
     */
    public HttpStreamManagerOptions withExpectedProtocol(HttpVersion expectedProtocol) {
        this.expectedProtocol = expectedProtocol;
        return this;
    }

    /**
     * @return The expected protocol for whole stream manager
     */
    public HttpVersion getExpectedProtocol() {
        return expectedProtocol;
    }
}
