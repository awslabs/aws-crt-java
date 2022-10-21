/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.StandardRetryOptions;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;

public class S3ClientOptions {

    private String endpoint;
    private String region;
    private ClientBootstrap clientBootstrap;
    private TlsContext tlsContext;
    private CredentialsProvider credentialsProvider;
    private long partSize;
    private double throughputTargetGbps;
    private boolean readBackpressureEnabled;
    private long initialReadWindowSize;
    private int maxConnections;
    /**
     * For multi-part upload, content-md5 will be calculated if the
     * computeContentMd5 is set to true.
     *
     * For single-part upload, leave the md5 header as-is if it was specified. If
     * the header is not set by in the initial request, it will calculated, when the
     * computeContentMd5 is set to true.
     *
     * Default is false;
     */
    private Boolean computeContentMd5;
    private StandardRetryOptions standardRetryOptions;

    public S3ClientOptions() {
        this.computeContentMd5 = false;
    }

    public S3ClientOptions withRegion(String region) {
        this.region = region;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public S3ClientOptions withClientBootstrap(ClientBootstrap clientBootstrap) {
        this.clientBootstrap = clientBootstrap;
        return this;
    }

    public ClientBootstrap getClientBootstrap() {
        return clientBootstrap;
    }

    public S3ClientOptions withCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public S3ClientOptions withPartSize(long partSize) {
        this.partSize = partSize;
        return this;
    }

    public long getPartSize() {
        return partSize;
    }

    public S3ClientOptions withThroughputTargetGbps(double throughputTargetGbps) {
        this.throughputTargetGbps = throughputTargetGbps;
        return this;
    }

    public double getThroughputTargetGbps() {
        return throughputTargetGbps;
    }

    /**
     * Set whether backpressure is enabled (false by default), to prevent response data downloading faster than you can handle it.
     * <p>
     * If false, no backpressure is applied and data will download as fast as possible.
     * <p>
     * If true, each S3MetaRequest has a flow-control window that shrinks as
     * response body data is downloaded (headers do not affect the window).
     * {@link #withInitialReadWindowSize} determines the starting size of each S3MetaRequest's window, in bytes.
     * Data stops downloading data whenever the window reaches zero.
     * Increment the window to keep data flowing by calling {@link S3MetaRequest#incrementReadWindow},
     * or by returning a size from {@link S3MetaRequestResponseHandler#onResponseBody}.
     * Maintain a larger window to keep up a high download throughput,
     * parts cannot download in parallel unless the window is large enough to hold multiple parts.
     * Maintain a smaller window to limit the amount of data buffered in memory.
     * <p>
     * WARNING: This feature is experimental.
     * Currently, backpressure is only applied to GetObject requests which are split into multiple parts,
     * and you may still receive some data after the window reaches zero.
     *
     * @param enable whether to enable or disable backpressure
     * @return this
     */
    public S3ClientOptions withReadBackpressureEnabled(boolean enable) {
        this.readBackpressureEnabled = enable;
        return this;
    }

    public boolean getReadBackpressureEnabled() {
        return this.readBackpressureEnabled;
    }

    /**
     * The starting size of each S3MetaRequest's flow-control window (if backpressure is enabled).
     *
     * @see #withReadBackpressureEnabled
     *
     * @param bytes size in bytes
     * @return this
     */
    public S3ClientOptions withInitialReadWindowSize(long bytes) {
        initialReadWindowSize = bytes;
        return this;
    }

    public long getInitialReadWindowSize() {
        return this.initialReadWindowSize;
    }

    public S3ClientOptions withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public S3ClientOptions withTlsContext(TlsContext tlsContext) {
        this.tlsContext = tlsContext;
        return this;
    }

    public TlsContext getTlsContext() {
        return tlsContext;
    }

    public S3ClientOptions withMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public S3ClientOptions withComputeContentMd5(Boolean computeContentMd5) {
        this.computeContentMd5 = computeContentMd5;
        return this;
    }

    public Boolean getComputeContentMd5() {
        return computeContentMd5;
    }

    public S3ClientOptions withStandardRetryOptions(StandardRetryOptions standardRetryOptions) {
        this.standardRetryOptions = standardRetryOptions;
        return this;
    }

    public StandardRetryOptions getStandardRetryOptions() {
        return this.standardRetryOptions;
    }
}
