/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.http.HttpMonitoringOptions;
import software.amazon.awssdk.crt.http.HttpProxyEnvironmentVariableOptions;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
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

    /**
     * Optional.
     * Proxy configuration for http connection.
     */
    private HttpProxyOptions proxyOptions;

    /**
     * Optional.
     * Configuration for fetching proxy configuration from environment.
     * By Default proxy_ev_settings.aws_http_proxy_env_var_type is set to
     * AWS_HPEV_ENABLE which means read proxy
     * configuration from environment.
     * Only works when proxy_options is not set. If both are set, configuration from
     * proxy_options is used.
     */
    private HttpProxyEnvironmentVariableOptions proxyEnvironmentVariableOptions;

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

    /**
     * Optional.
     * Configuration options for connection monitoring.
     * If the transfer speed falls below the specified
     * minimum_throughput_bytes_per_second, the operation is aborted.
     */
    private HttpMonitoringOptions monitoringOptions;

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

    public S3ClientOptions withProxyEnvironmentVariableOptions(
            HttpProxyEnvironmentVariableOptions proxyEnvironmentVariableOptions) {
        this.proxyEnvironmentVariableOptions = proxyEnvironmentVariableOptions;
        return this;
    }

    public HttpProxyEnvironmentVariableOptions getProxyEnvironmentVariableOptions() {
        return proxyEnvironmentVariableOptions;
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

    public S3ClientOptions withHttpMonitoringOptions(HttpMonitoringOptions monitoringOptions) {
        this.monitoringOptions = monitoringOptions;
        return this;
    }

    public HttpMonitoringOptions getMonitoringOptions() {
        return monitoringOptions;
    }
}
