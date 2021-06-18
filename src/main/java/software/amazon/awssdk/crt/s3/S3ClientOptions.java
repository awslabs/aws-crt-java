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
    private int maxConnections;
    private StandardRetryOptions standardRetryOptions;
    private S3ClientNativeCallbacks nativeCallbacks;

    public S3ClientOptions() {

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

    public S3ClientOptions withStandardRetryOptions(StandardRetryOptions standardRetryOptions) {
        this.standardRetryOptions = standardRetryOptions;
        return this;
    }

    public StandardRetryOptions getStandardRetryOptions() {
        return this.standardRetryOptions;
    }

    public S3ClientOptions withNativeCallbacks(S3ClientNativeCallbacks nativeCallbacks) {
        this.nativeCallbacks = nativeCallbacks;
        return this;
    }

    public S3ClientNativeCallbacks getNativeCallbacks() {
        return this.nativeCallbacks;
    }
}
