
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
    /** Client-owned direct buffer pool, or null. Closed in onShutdownComplete. */
    private final S3DirectBufferPool directBufferPool;

    /**
     * Upload-sizing inputs for the direct-buffer-pool pre-checks in
     * makeMetaRequest: whether the customer set partSize, and the
     * client's multipart threshold (0 = native default).
     */
    private final boolean partSizeExplicit;
    private final long clientMultipartUploadThreshold;

    public S3Client(S3ClientOptions options) throws CrtRuntimeException {
        TlsContext tlsCtx = options.getTlsContext();
        region = options.getRegion();

        // TODO - THIS SHOULD BE REMOVED ONCE BENCHMARKING IS DONE
        // Benchmark-only: auto-attach DBZ pool when -Daws.crt.s3.use_dbz=true is set
        // AND the caller didn't attach a pool. Lets the SDK's S3CrtAsyncClient path
        // (which doesn't yet expose DBZ APIs) participate in DBZ benchmarks.
        if (options.getDirectBufferPoolOptions() == null
                && "true".equalsIgnoreCase(System.getProperty("aws.crt.s3.use_dbz"))) {
            options.withDirectBufferPoolOptions(S3DirectBufferPoolOptions.auto());
        }

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

        // A pool switches the memory source from the native
        // default_buffer_pool to a JVM-owned pool. The client creates and
        // owns it: closed in onShutdownComplete, or in the catch below if
        // native client creation fails. Nothing between here and the try
        // can throw, so the pool cannot be orphaned.
        S3DirectBufferPoolOptions poolOptions = options.getDirectBufferPoolOptions();
        directBufferPool = poolOptions != null ? S3DirectBufferPool.fromOptions(poolOptions, options) : null;
        if (directBufferPool != null) {
            Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool created: ceiling = " + directBufferPool.ceilingBytes()
              + " bytes, part size = " + directBufferPool.partSize() + " bytes");
        }

        partSizeExplicit = options.getPartSize() > 0;
        clientMultipartUploadThreshold = options.getMultiPartUploadThreshold();

        // With a pool, hand native the part size and memory limit the pool
        // was sized from:
        // - memory limit (fixed/elastic): always the ceiling, even when the
        //   customer set a larger memoryLimitInBytes (fromOptions rejects a
        //   smaller one). The native client sizes download ranges and checks
        //   part sizes against its limit, so it must see the pool's real
        //   capacity; the customer's limit is still respected, since the
        //   client uses no more than the ceiling. auto() pools leave it to
        //   the native client, which resolves memoryLimitInBytes, the env
        //   vars and the tier default exactly as the pool did (and fails
        //   client creation on a malformed env var, as it does without a
        //   pool), unless the pool shrank its default to fit direct memory;
        //   then the native client gets the smaller ceiling too.
        // - part size: a pool that cannot grow cannot allocate dedicated
        //   buffers for ranges beyond the pool's maxGroupBytes(), so pin the
        //   part size unless the customer set one (disabling the native
        //   client's automatic download range sizing, which only runs when
        //   no part size is set).
        long nativePartSize = options.getPartSize();
        long nativeMemoryLimit = options.getMemoryLimitInBytes();
        if (directBufferPool != null) {
            if (!partSizeExplicit && !directBufferPool.servesOversize()) {
                nativePartSize = directBufferPool.partSize();
            }
            boolean autoMode = options.getDirectBufferPoolOptions().getMode() == S3DirectBufferPoolOptions.Mode.AUTO;
            if (!autoMode || (nativeMemoryLimit <= 0 && directBufferPool.ceilingClamped())) {
                nativeMemoryLimit = directBufferPool.ceilingBytes();
            }
        }

        try {
            acquireNativeHandle(s3ClientNew(this,
                    region.getBytes(UTF8),
                    options.getClientBootstrap().getNativeHandle(),
                    tlsCtx != null ? tlsCtx.getNativeHandle() : 0,
                    signingConfig,
                    nativePartSize,
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
                    nativeMemoryLimit,
                    fioOptionsSet,
                    shouldStream,
                    diskThroughputGbps,
                    directIo,
                    directBufferPool));
        } catch (RuntimeException | Error e) {
            // No native client means no shutdown callback: free the pool here.
            if (directBufferPool != null) {
                directBufferPool.close();
            }
            throw e;
        }

        addReferenceTo(options.getClientBootstrap());
        if(didCreateSigningConfig) {
            /* The native code will keep the needed resource around */
            signingConfig.close();
        }
    }

    private void onShutdownComplete() {
        // No meta request can reserve pool memory after shutdown. Frees all
        // unused pool memory now; memory held by unclosed S3BorrowedBuffers
        // is freed as each one closes.
        if (directBufferPool != null) {
            directBufferPool.close();
        }
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

        if (options.getChecksumConfig() != null && options.getChecksumAlgorithm() == ChecksumAlgorithm.MD5) {
            Log.log(Log.LogLevel.Error, Log.LogSubject.S3Client,
                    "S3Client.makeMetaRequest has invalid options; MD5 not supported as checksum algorithm.");
            throw new IllegalArgumentException("S3Client.makeMetaRequest has invalid options; MD5 not supported as checksum algorithm.");
        }

        if (directBufferPool != null) {
            checkUploadPartSizeForBufferPool(options);
        }

        S3MetaRequest metaRequest = new S3MetaRequest();
        S3MetaRequestResponseHandlerNativeAdapter responseHandlerNativeAdapter = new S3MetaRequestResponseHandlerNativeAdapter(
                options.getResponseHandler(), directBufferPool != null);

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
                directBufferPool != null ? directBufferPool.nativePoolState() : 0L,
                responseHandlerNativeAdapter.getSupportsBorrowedBufferOverload());

        metaRequest.setMetaRequestNativeHandle(metaRequestNativeHandle);

        if(didCreateSigningConfig) {
            /* The native code will keep the needed resource around */
            signingConfig.close();
        }
        return metaRequest;
    }

    /** S3's maximum number of parts per multipart upload. */
    private static final long MAX_UPLOAD_PARTS = 10_000;
    /** S3's minimum multipart upload part size. */
    private static final long MIN_UPLOAD_PART_SIZE = 5 * SizeUnits.MIB;
    /** S3's maximum upload part size. */
    private static final long MAX_UPLOAD_PART_SIZE = 5 * SizeUnits.GIB;

    /**
     * With a direct buffer pool, fail a multipart upload up front when
     * the native client would change its part size behind the customer's
     * back or beyond what the pool can hold. Without a pool, it silently
     * raises the part size (to at least 5 MiB, and to stay within 10,000
     * parts); with one, we refuse instead when the required part size is
     * larger than the current one and:
     * <ul>
     *   <li>the customer set {@code partSize} explicitly; or</li>
     *   <li>the pool is fixed and the part would not fit in
     *       {@link S3DirectBufferPool#maxGroupBytes()} (4 parts); or</li>
     *   <li>the pool can grow and the part exceeds half its ceiling, or
     *       5 GiB (the native client's own limit).</li>
     * </ul>
     * Skipped when the content length is unknown or a resume token is used
     * (the token's part size is honored).
     */
    private void checkUploadPartSizeForBufferPool(S3MetaRequestOptions options) {
        if (options.getMetaRequestType() != S3MetaRequestOptions.MetaRequestType.PUT_OBJECT
                || options.getResumeToken() != null) {
            return;
        }
        long contentLength = uploadContentLength(options);
        if (contentLength < 0) {
            return;
        }
        long partSize = directBufferPool.partSize();
        long threshold = clientMultipartUploadThreshold > 0
                ? clientMultipartUploadThreshold
                : Math.max(partSize, MIN_UPLOAD_PART_SIZE);
        if (contentLength <= threshold) {
            return;     // single PUT; part size does not apply
        }
        long required = Math.max(MIN_UPLOAD_PART_SIZE,
                (contentLength + MAX_UPLOAD_PARTS - 1) / MAX_UPLOAD_PARTS);
        if (required <= partSize) {
            return;
        }
        String need = "Uploading " + contentLength + " bytes needs a part size of at least " + required
                + " bytes (S3 allows at most " + MAX_UPLOAD_PARTS + " parts of at least "
                + MIN_UPLOAD_PART_SIZE + " bytes), but the part size is " + partSize + " bytes. ";
        if (partSizeExplicit) {
            throw new IllegalArgumentException(need
                + "With a direct buffer pool the explicitly configured partSize is never raised "
                + "automatically; set S3ClientOptions.withPartSize to at least " + required + ".");
        }
        if (!directBufferPool.servesOversize()) {
            if (required > directBufferPool.maxGroupBytes()) {
                throw new IllegalArgumentException(need
                    + "A fixed() direct buffer pool holds parts of at most " + directBufferPool.maxGroupBytes()
                    + " bytes; use S3DirectBufferPoolOptions.auto() or elastic(), or set "
                    + "S3ClientOptions.withPartSize to at least " + required + ".");
            }
            return;
        }
        long limit = Math.min(directBufferPool.ceilingBytes() / 2, MAX_UPLOAD_PART_SIZE);
        if (required > limit) {
            throw new IllegalArgumentException(need
                + "The largest part this direct buffer pool allows is " + limit + " bytes (half its "
                + directBufferPool.ceilingBytes() + "-byte ceiling, capped at " + MAX_UPLOAD_PART_SIZE
                + "); raise the pool's memory limit.");
        }
    }

    /** Content length of an upload from its file or Content-Length header, or -1 if unknown. */
    private static long uploadContentLength(S3MetaRequestOptions options) {
        if (options.getRequestFilePath() != null) {
            // java.io.File, not java.nio.file.Files (Android API 26+; min is 24).
            try {
                java.io.File file = new java.io.File(options.getRequestFilePath().toString());
                return file.isFile() ? file.length() : -1;  // native reports the file error
            } catch (SecurityException e) {
                return -1;
            }
        }
        for (software.amazon.awssdk.crt.http.HttpHeader header : options.getHttpRequest().getHeaders()) {
            if ("Content-Length".equalsIgnoreCase(header.getName())) {
                try {
                    return Long.parseLong(header.getValue().trim());
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
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

    /**
     * @return the maximum number of connections this client will keep active at once, across every endpoint it
     * talks to. The same value is additionally applied as the cap for each individual endpoint's connection pool.
     * Fixed for the lifetime of the client - derived from its configured throughput target, which
     * {@link S3ClientOptions#withMaxConnections} can lower but not raise.
     */
    public int getMaxActiveConnections() {
        return s3ClientGetMaxActiveConnections(getNativeHandle());
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
            boolean directIo,
            S3DirectBufferPool directByteBufferPool) throws CrtRuntimeException;

    private static native void s3ClientDestroy(long client);

    private static native int s3ClientGetMaxActiveConnections(long client);

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
            long directBufferPoolState,
            boolean usesBorrowedBufferOverload);

    /**
     * Returns the default native buffer pool's memory limit (bytes) for the
     * given throughput target ({@code 0} = EC2 auto-detect). Package-private,
     * used by {@code S3DirectBufferPool} automatic sizing.
     */
    static native long defaultMemoryLimitForThroughput(double throughputTargetGbps);
}
