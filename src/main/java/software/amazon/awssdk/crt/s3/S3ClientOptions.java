/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.http.HttpMonitoringOptions;
import software.amazon.awssdk.crt.http.HttpProxyEnvironmentVariableSetting;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.StandardRetryOptions;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig.AwsSigningAlgorithm;

public class S3ClientOptions {

    private String endpoint;
    private String region;
    private ClientBootstrap clientBootstrap;
    private TlsContext tlsContext;
    private CredentialsProvider credentialsProvider;
    private AwsSigningConfig signingConfig;
    private long partSize;
    private long multipartUploadThreshold;
    private double throughputTargetGbps;
    private boolean readBackpressureEnabled;
    private long initialReadWindowSize;
    private int maxConnections;
    private boolean enableS3Express;
    private long memoryLimitInBytes;
    private S3ExpressCredentialsProviderFactory s3expressCredentialsProviderFactory;
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

    /**
     * Optional.
     * Proxy configuration for http connection.
     */
    private HttpProxyOptions proxyOptions;

    /**
     * Optional.
     * Configuration for fetching proxy configuration from environment.
     * By Default read proxy configuration from environment is enabled.
     * Only works when proxyOptions is not set. If both are set, configuration from
     * proxy_options is used.
     */
    private HttpProxyEnvironmentVariableSetting httpProxyEnvironmentVariableSetting;

    /**
     * Optional.
     * If set to 0, default value is used.
     */
    private int connectTimeoutMs;

    /**
     * Optional.
     * Set keepalive to periodically transmit messages for detecting a disconnected
     * peer.
     */
    private S3TcpKeepAliveOptions tcpKeepAliveOptions;

    private HttpMonitoringOptions monitoringOptions;

    /**
     * Optional.
     * Controls how client performs file I/O operations. Only applies to file-based workloads.
     */
    private FileIoOptions fileIoOptions;

    /**
     * Optional Java-owned direct buffer pool. When set, S3 download
     * responses bypass the default byte[]-copy delivery path and
     * instead deliver the response body as a {@link java.nio.ByteBuffer}
     * slice over pool-owned memory.
     *
     * <p>Construct the pool via one of:</p>
     * <ul>
     *   <li>{@link S3DirectBufferPool#create(long, int)} — fixed-size
     *       (eager). Pool memory is fully committed at construction.</li>
     *   <li>{@link S3DirectBufferPool#createElastic(int, int, int)} —
     *       elastic. Memory tracks demand between {@code initialSlots}
     *       and {@code maxSlots}; lazy growth on {@code acquireSlot}.</li>
     * </ul>
     *
     * <p>See {@link S3DirectBufferPool} for the lifetime contract:
     * the ByteBuffer delivered to your handler is valid <strong>only
     * during the call</strong>. Copy out before returning if you need
     * to retain the bytes.</p>
     *
     * <p><b>Sizing JVM direct memory:</b> set
     * {@code -XX:MaxDirectMemorySize} to at least
     * {@code maxSlots × partSize × 1.5} regardless of the factory
     * used. For the fixed-size factory {@code maxSlots == slotCount},
     * so the formula collapses to {@code slotCount × partSize × 1.5}.</p>
     *
     * <p>Default: {@code null} — the existing byte[]-copy path is used,
     * exactly as before this option was introduced.</p>
     */
    private S3DirectBufferPool directByteBufferPool;

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

    /**
     * @deprecated Please use {@link #withSigningConfig(AwsSigningConfig)} instead.
     * The credentials provider will be used to create the signing Config when the client was created.
     * Client will use `AwsSigningConfig.getDefaultS3SigningConfig(region, credentialsProvider);` to create the signing config.
     *
     * @param credentialsProvider provide credentials for signing.
     * @return this
     */
    public S3ClientOptions withCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    /**
     * The configuration related to signing used by S3 client.
     * `AwsSigningConfig.getDefaultS3SigningConfig(region, credentialsProvider);` can be used as helper to create the default configuration to be used for S3.
     * In case of public object, or the http message already has a presigned URL, signing can be skipped.
     *
     * If not set, a default config will be used with anonymous credentials and skip signing the request.
     * If set:
     *  - Credentials provider is required. Other configs are all optional, and will be default to what
     *      needs to sign the request for S3, only overrides when Non-zero/Not-empty is set.
     *  - S3 Client will derive the right config for signing process based on this.
     *
     * Notes:
     * - For SIGV4_S3EXPRESS, S3 client will use the credentials in the config to derive the S3 Express
     *      credentials that are used in the signing process.
     * - Client may make modifications to signing config before passing it on to signer.
     *
     * @param signingConfig configuration related to signing via an AWS signing process.
     * @return this
     */
    public S3ClientOptions withSigningConfig(AwsSigningConfig signingConfig) {
        this.signingConfig = signingConfig;
        return this;
    }

    public AwsSigningConfig getSigningConfig() {
        return signingConfig;
    }

    /**
     * Sets the size, in bytes, of parts that files will be downloaded or uploaded in.
     * If not set, a dynamic default part size will be used based on the throughputTargetGbps, memoryLimitInBytes and initialReadWindowSize
     * to utilize the available resource and get the best performance.
     *
     * Notes: For PUT_OBJECT requests, the client will automatically adjust the part size to meet service limits:
     *   - Maximum number of parts per upload is 10,000
     *   - Minimum upload part size is 5 MiB
     *
     * @param partSize size in bytes of parts for downloads and uploads
     * @return this
     */
    public S3ClientOptions withPartSize(long partSize) {
        this.partSize = partSize;
        return this;
    }

    public long getPartSize() {
        return partSize;
    }

    public S3ClientOptions withMultipartUploadThreshold(long multipartUploadThreshold) {
        this.multipartUploadThreshold = multipartUploadThreshold;
        return this;
    }

    public long getMultiPartUploadThreshold() {
        return multipartUploadThreshold;
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
     * You will not receive any data after the window reaches zero until the window incremented.
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

    /*
     * @deprecated does not have any effect. Use endpoint option or add Host
     * header to meta request in order to specify endpoint.
     */
    @Deprecated
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

    public S3ClientOptions withProxyOptions(HttpProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    public HttpProxyOptions getProxyOptions() {
        return proxyOptions;
    }

    public S3ClientOptions withProxyEnvironmentVariableSetting(
            HttpProxyEnvironmentVariableSetting httpProxyEnvironmentVariableSetting) {
        this.httpProxyEnvironmentVariableSetting = httpProxyEnvironmentVariableSetting;
        return this;
    }

    public HttpProxyEnvironmentVariableSetting getHttpProxyEnvironmentVariableSetting() {
        return httpProxyEnvironmentVariableSetting;
    }

    public S3ClientOptions withConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        return this;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public S3ClientOptions withS3TcpKeepAliveOptions(S3TcpKeepAliveOptions tcpKeepAliveOptions) {
        this.tcpKeepAliveOptions = tcpKeepAliveOptions;
        return this;
    }

    public S3TcpKeepAliveOptions getTcpKeepAliveOptions() {
        return tcpKeepAliveOptions;
    }

    /**
     * Options for detecting bad HTTP connections.
     * If the transfer throughput falls below the specified thresholds
     * for long enough, the operation is retried on a new connection.
     * If left unset, default values are used.
     *
     * @param monitoringOptions monitoring options
     * @return this
     */
    public S3ClientOptions withHttpMonitoringOptions(HttpMonitoringOptions monitoringOptions) {
        this.monitoringOptions = monitoringOptions;
        return this;
    }

    public HttpMonitoringOptions getMonitoringOptions() {
        return monitoringOptions;
    }

    /**
     * To enable S3 Express support for client
     * The typical usage for a S3 Express request is to set this to true and let the request to be signed with
     * {@link AwsSigningAlgorithm#SIGV4_S3EXPRESS}, either from the client level signingConfig or override from request.
     *
     * @param enableS3Express To enable S3 Express support for client
     * @return this
     */
    public S3ClientOptions withEnableS3Express(boolean enableS3Express) {
        this.enableS3Express = enableS3Express;
        return this;
    }

    public boolean getEnableS3Express() {
        return enableS3Express;
    }

    public S3ClientOptions withS3ExpressCredentialsProviderFactory(S3ExpressCredentialsProviderFactory s3expressCredentialsProviderFactory) {
        this.s3expressCredentialsProviderFactory = s3expressCredentialsProviderFactory;
        return this;
    }

    public S3ExpressCredentialsProviderFactory getS3ExpressCredentialsProviderFactory() {
        return s3expressCredentialsProviderFactory;
    }

    /**
     * The amount of memory the CRT client is allowed to use.
     * The client makes a best-effort attempt at memory limiting but might exceed this limit in some cases.
     * If not provided, the client calculates this optimally from other settings, such as targetThroughput.
     * On a 64-bit system, the default is between 2Gib-24Gib.
     * It must be at least 1GiB and will be capped to SIZE_MAX of the system.
     * @param memoryLimitBytes Memory limit in bytes.
     * @return this
     */
    public S3ClientOptions withMemoryLimitInBytes(long memoryLimitBytes) {
        this.memoryLimitInBytes = memoryLimitBytes;
        return this;
    }

    /**
     * Retrieves the memory limit set for the CRT client in bytes.
     * If not set, this will return 0.
     * @return long memory limit in bytes
     */
    public long getMemoryLimitInBytes() {
        return memoryLimitInBytes;
    }

    /**
     * Sets the file I/O options for controlling how client performs file I/O operations.
     *
     *  Notes: This only applies to the requests that `withRequestFilePath` was set.
     *
     * @param fileIoOptions the file I/O options to use
     * @return this
     */
    public S3ClientOptions withFileIoOptions(FileIoOptions fileIoOptions) {
        this.fileIoOptions = fileIoOptions;
        return this;
    }

    /**
     * Gets the file I/O options for controlling how client performs file I/O operations.
     *
     * @return the file I/O options, or null if not set
     */
    public FileIoOptions getFileIoOptions() {
        return fileIoOptions;
    }

    /**
     * Sets a Java-owned direct buffer pool for zero-copy response body delivery.
     *
     * <p>When set, S3 download responses deliver body bytes as a
     * {@link java.nio.ByteBuffer} slice over pool-owned off-heap memory,
     * eliminating the {@code byte[]} allocation and copy on the hot path.</p>
     *
     * @param pool the direct buffer pool, or {@code null} to use the default byte[]-copy path
     * @return this
     * @see S3DirectBufferPool
     */
    public S3ClientOptions withDirectByteBufferPool(S3DirectBufferPool pool) {
        this.directByteBufferPool = pool;
        return this;
    }

    /**
     * Returns the configured direct buffer pool, or {@code null} if not set.
     *
     * @return the direct buffer pool or null
     */
    public S3DirectBufferPool getDirectByteBufferPool() {
        return directByteBufferPool;
    }
}
