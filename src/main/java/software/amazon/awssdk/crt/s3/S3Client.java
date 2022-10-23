
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

        HttpProxyOptions proxyOptions = options.getProxyOptions();
        if (proxyOptions != null) {
            proxyConnectionType = proxyOptions.getConnectionType().getValue();
            proxyHost = proxyOptions.getHost();
            proxyPort = proxyOptions.getPort();
            proxyTlsContext = proxyOptions.getTlsContext();
            proxyAuthorizationType = proxyOptions.getAuthorizationType().getValue();
            proxyAuthorizationUsername = proxyOptions.getAuthorizationUsername();
            proxyAuthorizationPassword = proxyOptions.getAuthorizationPassword();
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

        acquireNativeHandle(s3ClientNew(this,
                region.getBytes(UTF8),
                options.getEndpoint() != null ? options.getEndpoint().getBytes(UTF8) : null,
                options.getClientBootstrap().getNativeHandle(),
                tlsCtx != null ? tlsCtx.getNativeHandle() : 0,
                options.getCredentialsProvider().getNativeHandle(),
                options.getPartSize(),
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
                environmentVariableProxyConnectionType,
                environmentVariableProxyTlsConnectionOptions != null
                        ? environmentVariableProxyTlsConnectionOptions.getNativeHandle()
                        : 0,
                environmentVariableType,
                options.getConnectTimeoutMs(),
                options.getTcpKeepAliveOptions(),
                monitoringThroughputThresholdInBytesPerSecond,
                monitoringFailureIntervalInSeconds));

        addReferenceTo(options.getClientBootstrap());
        addReferenceTo(options.getCredentialsProvider());
    }

    private void onShutdownComplete() {
        releaseReferences();

        this.shutdownComplete.complete(null);
    }

    public S3MetaRequest makeMetaRequest(S3MetaRequestOptions options) {

        if (options.getHttpRequest() == null) {
            Log.log(Log.LogLevel.Error, Log.LogSubject.S3Client,
                    "S3Client.makeMetaRequest has invalid options; Http Request cannot be null.");
            return null;
        }

        if (options.getResponseHandler() == null) {
            Log.log(Log.LogLevel.Error, Log.LogSubject.S3Client,
                    "S3Client.makeMetaRequest has invalid options; Response Handler cannot be null.");
            return null;
        }

        S3MetaRequest metaRequest = new S3MetaRequest();
        S3MetaRequestResponseHandlerNativeAdapter responseHandlerNativeAdapter = new S3MetaRequestResponseHandlerNativeAdapter(
                options.getResponseHandler());

        byte[] httpRequestBytes = options.getHttpRequest().marshalForJni();
        long credentialsProviderNativeHandle = 0;
        if (options.getCredentialsProvider() != null) {
            credentialsProviderNativeHandle = options.getCredentialsProvider().getNativeHandle();
        }
        URI endpoint = options.getEndpoint();

        byte[] resumeToken = options.getResumeToken() == null ? null : options.getResumeToken().getBytes(UTF8);

        int checksumAlgorithm = options.getChecksumAlgorithm() != null ? options.getChecksumAlgorithm().getNativeValue() : ChecksumAlgorithm.NONE.getNativeValue();

        long metaRequestNativeHandle = s3ClientMakeMetaRequest(getNativeHandle(), metaRequest, region.getBytes(UTF8),
                options.getMetaRequestType().getNativeValue(), checksumAlgorithm, options.getValidateChecksum(), httpRequestBytes,
                options.getHttpRequest().getBodyStream(), credentialsProviderNativeHandle,
                responseHandlerNativeAdapter, endpoint == null ? null : endpoint.toString().getBytes(UTF8), resumeToken);

        metaRequest.setMetaRequestNativeHandle(metaRequestNativeHandle);
        if (credentialsProviderNativeHandle != 0) {
            /*
             * Keep the java object alive until the meta Request shut down and release all
             * the resources it's pointing to
             */
            metaRequest.addReferenceTo(options.getCredentialsProvider());
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
    private static native long s3ClientNew(S3Client thisObj, byte[] region, byte[] endpoint, long clientBootstrap,
            long tlsContext, long signingConfig, long partSize, double throughputTargetGbps,
            boolean enableReadBackpressure, long initialReadWindow, int maxConnections,
            StandardRetryOptions standardRetryOptions, boolean computeContentMd5,
            int proxyConnectionType,
            byte[] proxyHost,
            int proxyPort,
            long proxyTlsContext,
            int proxyAuthorizationType,
            byte[] proxyAuthorizationUsername,
            byte[] proxyAuthorizationPassword,
            int environmentVariableProxyConnectionType,
            long environmentVariableProxyTlsConnectionOptions,
            int environmentVariableSetting,
            int connectTimeoutMs,
            S3TcpKeepAliveOptions tcpKeepAliveOptions,
            long monitoringThroughputThresholdInBytesPerSecond,
            int monitoringFailureIntervalInSeconds) throws CrtRuntimeException;

    private static native void s3ClientDestroy(long client);

    private static native long s3ClientMakeMetaRequest(long clientId, S3MetaRequest metaRequest, byte[] region,
            int metaRequestType, int checksumAlgorithm, boolean validateChecksum, byte[] httpRequestBytes, HttpRequestBodyStream httpRequestBodyStream,
            long signingConfig, S3MetaRequestResponseHandlerNativeAdapter responseHandlerNativeAdapter,
            byte[] endpoint, byte[] resumeToken);
}
