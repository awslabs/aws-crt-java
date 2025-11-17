
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpMonitoringOptions;
import software.amazon.awssdk.crt.http.HttpProxyEnvironmentVariableSetting;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.io.TlsConnectionOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.StandardRetryOptions;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;

import java.net.URI;

public class S3Client extends CrtResource {

    private final static Charset UTF8 = java.nio.charset.StandardCharsets.UTF_8;
    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();
    private final String region;

    public S3Client(S3ClientOptions options) throws CrtRuntimeException {
        TlsContext tlsCtx = options.getTlsContext();
        region = options.getRegion();

        int proxyConnectionType = 0;
        String proxyHost = null;
        int proxyPort = 0;
        TlsContext proxyTlsContext = null;
        int proxyAuthorizationType = 0;
        String proxyAuthorizationUsername = null;
        String proxyAuthorizationPassword = null;
        String noProxyHosts = null;
        // Handle FileIoOptions from S3ClientOptions
        boolean fioOptionsSet = false;
        boolean shouldStream = false;
        double diskThroughputGbps = 0.0;
        boolean directIo = false;

        FileIoOptions fileIoOptions = options.getFileIoOptions();
        if (fileIoOptions != null) {
            fioOptionsSet = true;
            shouldStream = fileIoOptions.getShouldStream();
            diskThroughputGbps = fileIoOptions.getDiskThroughputGbps();
            directIo = fileIoOptions.getDirectIo();
        }

        HttpProxyOptions proxyOptions = options.getProxyOptions();
        if (proxyOptions != null) {
            proxyConnectionType = proxyOptions.getConnectionType().getValue();
            proxyHost = proxyOptions.getHost();
            proxyPort = proxyOptions.getPort();
            proxyTlsContext = proxyOptions.getTlsContext();
            proxyAuthorizationType = proxyOptions.getAuthorizationType().getValue();
            proxyAuthorizationUsername = proxyOptions.getAuthorizationUsername();
            proxyAuthorizationPassword = proxyOptions.getAuthorizationPassword();
            noProxyHosts = proxyOptions.getNoProxyHosts();
        }

        int environmentVariableProxyConnectionType = 0;
        TlsConnectionOptions environmentVariableProxyTlsConnectionOptions = null;
        int environmentVariableType = 1;
        HttpProxyEnvironmentVariableSetting environmentVariableSetting = options.getHttpProxyEnvironmentVariableSetting();
        if (environmentVariableSetting != null) {
            environmentVariableProxyConnectionType = environmentVariableSetting.getConnectionType().getValue();
            environmentVariableProxyTlsConnectionOptions = environmentVariableSetting.getTlsConnectionOptions();
            environmentVariableType = environmentVariableSetting.getEnvironmentVariableType().getValue();
        }

        HttpMonitoringOptions monitoringOptions = options.getMonitoringOptions();
        long monitoringThroughputThresholdInBytesPerSecond = 0;
        int monitoringFailureIntervalInSeconds = 0;
        if (monitoringOptions != null) {
            monitoringThroughputThresholdInBytesPerSecond = monitoringOptions.getMinThroughputBytesPerSecond();
            monitoringFailureIntervalInSeconds = monitoringOptions.getAllowableThroughputFailureIntervalSeconds();
        }
        AwsSigningConfig signingConfig = options.getSigningConfig();
        boolean didCreateSigningConfig = false;
        if(signingConfig == null && options.getCredentialsProvider()!= null) {
            /* Create the signing config from credentials provider */
            signingConfig = AwsSigningConfig.getDefaultS3SigningConfig(region, options.getCredentialsProvider());
            didCreateSigningConfig = true;
        }

        acquireNativeHandle(s3ClientNew(this,
                region.getBytes(UTF8),
                options.getClientBootstrap().getNativeHandle(),
                tlsCtx != null ? tlsCtx.getNativeHandle() : 0,
                signingConfig,
                options.getPartSize(),
                options.getMultiPartUploadThreshold(),
                options.getThroughputTargetGbps(),
                options.getReadBackpressureEnabled(),
                options.getInitialReadWindowSize(),
                options.getMaxConnections(),
                options.getStandardRetryOptions(),
                options.getComputeContentMd5(),
                proxyConnectionType,
                proxyHost != null ? proxyHost.getBytes(UTF8) : null,
                proxyPort,
                proxyTlsContext != null ? proxyTlsContext.getNativeHandle() : 0,
                proxyAuthorizationType,
                proxyAuthorizationUsername != null ? proxyAuthorizationUsername.getBytes(UTF8) : null,
                proxyAuthorizationPassword != null ? proxyAuthorizationPassword.getBytes(UTF8) : null,
                noProxyHosts != null ? noProxyHosts.getBytes(UTF8) : null,
                environmentVariableProxyConnectionType,
                environmentVariableProxyTlsConnectionOptions != null
                        ? environmentVariableProxyTlsConnectionOptions.getNativeHandle()
                        : 0,
                environmentVariableType,
                options.getConnectTimeoutMs(),
                options.getTcpKeepAliveOptions(),
                monitoringThroughputThresholdInBytesPerSecond,
                monitoringFailureIntervalInSeconds,
                options.getEnableS3Express(),
                options.getS3ExpressCredentialsProviderFactory(),
                options.getMemoryLimitInBytes(),
                fioOptionsSet,
                shouldStream,
                diskThroughputGbps,
                directIo));

        addReferenceTo(options.getClientBootstrap());
        if(didCreateSigningConfig) {
            /* The native code will keep the needed resource around */
            signingConfig.close();
        }
    }

    private void onShutdownComplete() {
        releaseReferences();

        this.shutdownComplete.complete(null);
    }

    public S3MetaRequest makeMetaRequest(S3MetaRequestOptions options) {

        if(isNull()) {
            Log.log(Log.LogLevel.Error, Log.LogSubject.S3Client,
                    "S3Client.makeMetaRequest has invalid client. The client can not be used after it is closed.");
            throw new IllegalStateException("S3Client.makeMetaRequest has invalid client. The client can not be used after it is closed.");
        }

        if (options.getHttpRequest() == null) {
            Log.log(Log.LogLevel.Error, Log.LogSubject.S3Client,
                    "S3Client.makeMetaRequest has invalid options; Http Request cannot be null.");
            throw new IllegalArgumentException("S3Client.makeMetaRequest has invalid options; Http Request cannot be null.");
        }

        if (options.getResponseHandler() == null) {
            Log.log(Log.LogLevel.Error, Log.LogSubject.S3Client,
                    "S3Client.makeMetaRequest has invalid options; Response Handler cannot be null.");
            throw new IllegalArgumentException("S3Client.makeMetaRequest has invalid options; Response Handler cannot be null.");
        }

        String operationName = options.getOperationName();
        if (options.getMetaRequestType() == S3MetaRequestOptions.MetaRequestType.DEFAULT && operationName == null) {
            Log.log(Log.LogLevel.Error, Log.LogSubject.S3Client,
                    "S3Client.makeMetaRequest has invalid options; Operation name must be set for MetaRequestType.DEFAULT.");
            throw new IllegalArgumentException("S3Client.makeMetaRequest has invalid options; Operation name must be set for MetaRequestType.DEFAULT.");
        }

        S3MetaRequest metaRequest = new S3MetaRequest();
        S3MetaRequestResponseHandlerNativeAdapter responseHandlerNativeAdapter = new S3MetaRequestResponseHandlerNativeAdapter(
                options.getResponseHandler());

        byte[] httpRequestBytes = options.getHttpRequest().marshalForJni();
        byte[] requestFilePath = null;
        if (options.getRequestFilePath() != null) {
            requestFilePath = options.getRequestFilePath().toString().getBytes(UTF8);
        }
        byte[] responseFilePath = null;
        if (options.getResponseFilePath() != null) {
            responseFilePath = options.getResponseFilePath().toString().getBytes(UTF8);
        }

        AwsSigningConfig signingConfig = options.getSigningConfig();
        boolean didCreateSigningConfig = false;
        if(signingConfig == null && options.getCredentialsProvider()!= null) {
            signingConfig = AwsSigningConfig.getDefaultS3SigningConfig(region, options.getCredentialsProvider());
            didCreateSigningConfig = true;
        }
        URI endpoint = options.getEndpoint();

        ChecksumConfig checksumConfig = options.getChecksumConfig() != null ? options.getChecksumConfig()
                : new ChecksumConfig();
        // Handle FileIoOptions from S3MetaRequestOptions
        boolean fioOptionsSet = false;
        boolean shouldStream = false;
        double diskThroughputGbps = 0.0;
        boolean directIo = false;

        FileIoOptions fileIoOptions = options.getFileIoOptions();
        if (fileIoOptions != null) {
            fioOptionsSet = true;
            shouldStream = fileIoOptions.getShouldStream();
            diskThroughputGbps = fileIoOptions.getDiskThroughputGbps();
            directIo = fileIoOptions.getDirectIo();
        }

        long metaRequestNativeHandle = s3ClientMakeMetaRequest(getNativeHandle(), metaRequest, region.getBytes(UTF8),
                options.getMetaRequestType().getNativeValue(),
                operationName == null ? null : operationName.getBytes(UTF8),
                checksumConfig.getChecksumLocation().getNativeValue(),
                checksumConfig.getChecksumAlgorithm().getNativeValue(), checksumConfig.getValidateChecksum(),
                ChecksumAlgorithm.marshallAlgorithmsForJNI(checksumConfig.getValidateChecksumAlgorithmList()),
                httpRequestBytes, options.getHttpRequest().getBodyStream(), requestFilePath, signingConfig,
                responseHandlerNativeAdapter, endpoint == null ? null : endpoint.toString().getBytes(UTF8),
                options.getResumeToken(), options.getObjectSizeHint(), responseFilePath,
                options.getResponseFileOption().getNativeValue(), options.getResponseFilePosition(),
                options.getResponseFileDeleteOnFailure(),
                fioOptionsSet,
                shouldStream,
                diskThroughputGbps,
                directIo,
                options.getForceDynamicPartSize());

        metaRequest.setMetaRequestNativeHandle(metaRequestNativeHandle);

        if(didCreateSigningConfig) {
            /* The native code will keep the needed resource around */
            signingConfig.close();
        }
        return metaRequest;
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the
     * native handle is released or if it waits. Resources that wait are responsible
     * for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() {
        return false;
    }

    /**
     * Cleans up the native resources associated with this client. The client is
     * unusable after this call
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            s3ClientDestroy(getNativeHandle());
        }
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() {
        return shutdownComplete;
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long s3ClientNew(S3Client thisObj, byte[] region, long clientBootstrap,
            long tlsContext, AwsSigningConfig signingConfig, long partSize, long multipartUploadThreshold, double throughputTargetGbps,
            boolean enableReadBackpressure, long initialReadWindow, int maxConnections,
            StandardRetryOptions standardRetryOptions, boolean computeContentMd5,
            int proxyConnectionType,
            byte[] proxyHost,
            int proxyPort,
            long proxyTlsContext,
            int proxyAuthorizationType,
            byte[] proxyAuthorizationUsername,
            byte[] proxyAuthorizationPassword,
            byte[] noProxyHosts,
            int environmentVariableProxyConnectionType,
            long environmentVariableProxyTlsConnectionOptions,
            int environmentVariableSetting,
            int connectTimeoutMs,
            S3TcpKeepAliveOptions tcpKeepAliveOptions,
            long monitoringThroughputThresholdInBytesPerSecond,
            int monitoringFailureIntervalInSeconds,
            boolean enableS3Express,
            S3ExpressCredentialsProviderFactory s3expressCredentialsProviderFactory,
            long memoryLimitInBytes,
            boolean fioOptionsSet,
            boolean shouldStream,
            double diskThroughputGbps,
            boolean directIo) throws CrtRuntimeException;

    private static native void s3ClientDestroy(long client);

    private static native long s3ClientMakeMetaRequest(long clientId, S3MetaRequest metaRequest, byte[] region,
            int metaRequestType, byte[] operationName,
            int checksumLocation, int checksumAlgorithm, boolean validateChecksum,
            int[] validateAlgorithms, byte[] httpRequestBytes,
            HttpRequestBodyStream httpRequestBodyStream, byte[] requestFilePath,
            AwsSigningConfig signingConfig, S3MetaRequestResponseHandlerNativeAdapter responseHandlerNativeAdapter,
            byte[] endpoint, ResumeToken resumeToken, Long objectSizeHint, byte[] responseFilePath,
            int responseFileOption, long responseFilePosition, boolean responseFileDeleteOnFailure,
            boolean fioOptionsSet,
            boolean shouldStream,
            double diskThroughputGbps,
            boolean directIo,
            boolean forceDynamicPartSize);
}
