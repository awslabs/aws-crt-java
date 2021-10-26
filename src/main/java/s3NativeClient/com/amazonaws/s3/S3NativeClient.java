/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package com.amazonaws.s3;

import com.amazonaws.s3.model.*;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.s3.CrtS3RuntimeException;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.StandardRetryOptions;
import software.amazon.awssdk.crt.io.Uri;
import software.amazon.awssdk.crt.s3.*;
import software.amazon.awssdk.crt.Log;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.lang.String;

public class S3NativeClient implements AutoCloseable {
    private final S3Client s3Client;
    private final String signingRegion;

    public S3NativeClient(final String signingRegion, final ClientBootstrap clientBootstrap,
            final CredentialsProvider credentialsProvider, final long partSizeBytes,
            final double targetThroughputGbps) {

        this(signingRegion, clientBootstrap, credentialsProvider, partSizeBytes, targetThroughputGbps, 0);
    }

    public S3NativeClient(final String signingRegion, final ClientBootstrap clientBootstrap,
            final CredentialsProvider credentialsProvider, final long partSizeBytes, final double targetThroughputGbps,
            final int maxConnections) {

        this(signingRegion, new S3Client(new S3ClientOptions().withClientBootstrap(clientBootstrap)
                .withCredentialsProvider(credentialsProvider).withRegion(signingRegion).withPartSize(partSizeBytes)
                .withThroughputTargetGbps(targetThroughputGbps).withMaxConnections(maxConnections)));
    }

    // TODO Builder class for S3NativeClient
    public S3NativeClient(final String signingRegion, final ClientBootstrap clientBootstrap,
            final CredentialsProvider credentialsProvider, final long partSizeBytes, final double targetThroughputGbps,
            final int maxConnections, final StandardRetryOptions retryOptions) {

        this(signingRegion,
                new S3Client(new S3ClientOptions().withClientBootstrap(clientBootstrap)
                        .withCredentialsProvider(credentialsProvider).withRegion(signingRegion)
                        .withPartSize(partSizeBytes).withThroughputTargetGbps(targetThroughputGbps)
                        .withMaxConnections(maxConnections).withStandardRetryOptions(retryOptions)));
    }

    public S3NativeClient(final String signingRegion, final S3Client s3Client) {
        // keep signing region to construct Host header per-request
        this.signingRegion = signingRegion;
        this.s3Client = s3Client;
    }

    private void addCustomHeaders(List<HttpHeader> headers, HttpHeader[] customHeaders) {
        assert headers != null : "Invalid argument - headers list is null";

        if (customHeaders == null || customHeaders.length == 0) {
            return;
        }

        for (HttpHeader customHeader : customHeaders) {

            // Prevent duplicates and warn if any are found.
            for (HttpHeader header : headers) {
                if (customHeader.getName().equals(header.getName())) {
                    Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                            "Custom header '" + customHeader.getName() + "' is overriding existing header.");
                    headers.remove(header);
                    break;
                }
            }

            headers.add(customHeader);
        }
    }

    private String getEncodedPath(String key, String customQueryParameters) {
        String encodedPath = Uri.appendEncodingUriPath("/", key);

        if (customQueryParameters == null) {
            return encodedPath;
        }

        String trimmedCustomQueryParameters = customQueryParameters.trim();

        if (trimmedCustomQueryParameters.equals("")) {
            return encodedPath;
        }

        encodedPath += "?" + trimmedCustomQueryParameters;
        return encodedPath;
    }

    private String urlParamBuild(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s", Uri.encodeUriParam(entry.getKey().toString()),
                    Uri.encodeUriParam(entry.getValue().toString())));
        }
        return sb.toString();
    }

    private String getGetObjectRequestQueryParameters(GetObjectRequest request) {
        final Map<String, String> requestParams = new HashMap<>();
        if (request.partNumber() != null) {
            requestParams.put("partNumber", Integer.toString(request.partNumber()));
        }
        if (request.responseCacheControl() != null) {
            requestParams.put("response-cache-control", request.responseCacheControl());
        }
        if (request.responseContentDisposition() != null) {
            requestParams.put("response-content-disposition", request.responseContentDisposition());
        }
        if (request.responseContentEncoding() != null) {
            requestParams.put("response-content-encoding", request.responseContentEncoding());
        }
        if (request.responseContentLanguage() != null) {
            requestParams.put("response-content-language", request.responseContentLanguage());
        }
        if (request.responseContentType() != null) {
            requestParams.put("response-content-type", request.responseContentType());
        }
        if (request.responseExpires() != null) {
            requestParams.put("response-expires",
                    DateTimeFormatter.RFC_1123_DATE_TIME.format(request.responseExpires()));
        }
        if (request.versionId() != null) {
            requestParams.put("versionId", request.versionId());
        }
        String queryParams = urlParamBuild(requestParams);
        if (!queryParams.trim().equals("")) {
            return queryParams + "&" + request.customQueryParameters();
        }
        return request.customQueryParameters();
    }

    private String getListObjectsV2RequestQueryParameters(final ListObjectsV2Request request) {
        final Map<String, String> requestParams = new HashMap<>();

        // list-type parameter indicates version 2 of the API, as described in
        // https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListObjectsV2.html
        requestParams.put("list-type", "2");

        if (request.continuationToken() != null) {
            requestParams.put("continuation-token", request.continuationToken());
        }
        if (request.delimiter() != null) {
            requestParams.put("delimiter", request.delimiter());
        }
        if (request.fetchOwner() != null && request.fetchOwner()) {
            requestParams.put("fetch-owner", "true");
        }
        if (request.maxKeys() != null) {
            requestParams.put("max-keys", request.maxKeys().toString());
        }
        if (request.prefix() != null) {
            requestParams.put("prefix", request.prefix());
        }
        if (request.startAfter() != null) {
            requestParams.put("start-after", request.startAfter());
        }
        if (request.expectedBucketOwner() != null) {
            requestParams.put("x-amz-expected-bucket-owner", request.expectedBucketOwner());
        }
        if (request.requestPayer() == RequestPayer.REQUESTER) {
            requestParams.put("x-amz-request-payer", "requester");
        }

        return urlParamBuild(requestParams);
    }

    private void addCancelCheckToFuture(CompletableFuture<?> future, final S3MetaRequest metaRequest) {
        metaRequest.addRef();

        future.whenComplete((r, t) -> {
            if (future.isCancelled()) {
                metaRequest.cancel();
            }
            metaRequest.close();
        });
    }

    public CompletableFuture<GetObjectOutput> getObject(GetObjectRequest request,
            final ResponseDataConsumer<GetObjectOutput> dataHandler) {
        final CompletableFuture<GetObjectOutput> resultFuture = new CompletableFuture<>();
        final GetObjectOutput.Builder resultBuilder = GetObjectOutput.builder();
        final S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
            private GetObjectOutput getObjectOutput;

            @Override
            public void onResponseHeaders(final int statusCode, final HttpHeader[] headers) {
                for (int headerIndex = 0; headerIndex < headers.length; ++headerIndex) {
                    try {
                        populateGetObjectOutputHeader(resultBuilder, headers[headerIndex]);
                    } catch (Exception e) {
                        resultFuture.completeExceptionally(new RuntimeException(
                                String.format(
                                        "Could not process response header {%s}: " + headers[headerIndex].getName()),
                                e));
                    }
                }
                dataHandler.onResponseHeaders(statusCode, headers);
                getObjectOutput = resultBuilder.build();
                dataHandler.onResponse(getObjectOutput);
            }

            @Override
            public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                dataHandler.onResponseData(bodyBytesIn);
                return 0;
            }

            @Override
            public void onFinished(int errorCode, int responseStatus, byte[] errorPayload) {
                CrtS3RuntimeException ex = null;
                try {
                    if (errorCode != CRT.AWS_CRT_SUCCESS) {
                        ex = new CrtS3RuntimeException(errorCode, responseStatus, errorPayload);
                        dataHandler.onException(ex);
                    } else {
                        dataHandler.onFinished();
                    }
                } catch (Exception e) { /* ignore user callback exception */
                } finally {
                    if (ex != null) {
                        resultFuture.completeExceptionally(ex);
                    } else {
                        resultFuture.complete(getObjectOutput);
                    }
                }
            }
        };

        List<HttpHeader> headers = new LinkedList<>();

        // TODO: additional logic needed for *special* partitions
        headers.add(new HttpHeader("Host", request.bucket() + ".s3." + signingRegion + ".amazonaws.com"));
        populateGetObjectRequestHeaders(header -> headers.add(header), request);

        addCustomHeaders(headers, request.customHeaders());
        String getObjectRequestQueryParameters = getGetObjectRequestQueryParameters(request);
        String encodedPath = getEncodedPath(request.key(), getObjectRequestQueryParameters);

        HttpRequest httpRequest = new HttpRequest("GET", encodedPath, headers.toArray(new HttpHeader[0]), null);

        S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                .withMetaRequestType(S3MetaRequestOptions.MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                .withResponseHandler(responseHandler);

        try (final S3MetaRequest metaRequest = s3Client.makeMetaRequest(metaRequestOptions)) {
            addCancelCheckToFuture(resultFuture, metaRequest);
            return resultFuture;
        }
    }

    public CompletableFuture<PutObjectOutput> putObject(PutObjectRequest request,
            final RequestDataSupplier requestDataSupplier) {
        final CompletableFuture<PutObjectOutput> resultFuture = new CompletableFuture<>();
        final PutObjectOutput.Builder resultBuilder = PutObjectOutput.builder();
        HttpRequestBodyStream payloadStream = new HttpRequestBodyStream() {
            @Override
            public boolean sendRequestBody(final ByteBuffer outBuffer) {
                try {
                    return requestDataSupplier.getRequestBytes(outBuffer);
                } catch (Exception e) {
                    resultFuture.completeExceptionally(e);
                    return true;
                }
            }

            @Override
            public boolean resetPosition() {
                try {
                    return requestDataSupplier.resetPosition();
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public long getLength() {
                return request.contentLength();
            }
        };

        final List<HttpHeader> headers = new LinkedList<>();

        // TODO: additional logic needed for *special* partitions
        headers.add(new HttpHeader("Host", request.bucket() + ".s3." + signingRegion + ".amazonaws.com"));
        populatePutObjectRequestHeaders(header -> headers.add(header), request);

        addCustomHeaders(headers, request.customHeaders());

        String encodedPath = getEncodedPath(request.key(), request.customQueryParameters());

        HttpRequest httpRequest = new HttpRequest("PUT", encodedPath, headers.toArray(new HttpHeader[0]),
                payloadStream);

        final S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
            @Override
            public void onResponseHeaders(final int statusCode, final HttpHeader[] headers) {
                for (int headerIndex = 0; headerIndex < headers.length; ++headerIndex) {
                    try {
                        populatePutObjectOutputHeader(resultBuilder, headers[headerIndex]);
                    } catch (Exception e) {
                        resultFuture.completeExceptionally(new RuntimeException(
                                String.format(
                                        "Could not process response header {%s}: " + headers[headerIndex].getName()),
                                e));
                    }
                }
                requestDataSupplier.onResponseHeaders(statusCode, headers);
            }

            @Override
            public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                return 0;
            }

            @Override
            public void onFinished(int errorCode, int responseStatus, byte[] errorPayload) {
                CrtS3RuntimeException ex = null;
                try {
                    if (errorCode != CRT.AWS_CRT_SUCCESS) {
                        ex = new CrtS3RuntimeException(errorCode, responseStatus, errorPayload);
                        requestDataSupplier.onException(ex);
                    } else {
                        requestDataSupplier.onFinished();
                    }
                } catch (Exception e) { /* ignore user callback exception */
                } finally {
                    if (ex != null) {
                        resultFuture.completeExceptionally(ex);
                    } else {
                        resultFuture.complete(resultBuilder.build());
                    }
                }
            }
        };

        S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                .withMetaRequestType(S3MetaRequestOptions.MetaRequestType.PUT_OBJECT).withHttpRequest(httpRequest)
                .withResponseHandler(responseHandler);

        try (final S3MetaRequest metaRequest = s3Client.makeMetaRequest(metaRequestOptions)) {
            addCancelCheckToFuture(resultFuture, metaRequest);
            return resultFuture;
        }
    }

    public CompletableFuture<ListObjectsV2Output> listObjectsV2(final ListObjectsV2Request request) {
        final CompletableFuture<ListObjectsV2Output> resultFuture = new CompletableFuture<>();
        final S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
            private ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
            private Exception bufferException = null;

            @Override
            public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                try {
                    memoryStream.write(bodyBytesIn.array());
                } catch (final IOException ex) {
                    bufferException = ex;
                }

                return 0;
            }

            @Override
            public void onFinished(int errorCode, int responseStatus, byte[] errorPayload) {
                if (errorCode != CRT.AWS_CRT_SUCCESS) {
                    CrtS3RuntimeException ex = new CrtS3RuntimeException(errorCode, responseStatus, errorPayload);
                    resultFuture.completeExceptionally(ex);
                } else if (bufferException != null) {
                    // TODO: convert exception?
                    resultFuture.completeExceptionally(bufferException);
                }  else {
                    final ByteArrayInputStream stream = new ByteArrayInputStream(memoryStream.toByteArray());
                    try {
                        final ListObjectsV2Output listObjectsV2Output = parseListObjectsV2Output(stream);
                        resultFuture.complete(listObjectsV2Output);
                    } catch (Exception ex) {
                        // TODO: convert exception?
                        resultFuture.completeExceptionally(ex);
                    }
                }
            }
        };

        List<HttpHeader> headers = new LinkedList<>();

        // TODO: additional logic needed for *special* partitions
        headers.add(new HttpHeader("Host", request.bucket() + ".s3." + signingRegion + ".amazonaws.com"));

        String requestQueryParameters = getListObjectsV2RequestQueryParameters(request);
        String encodedPath = getEncodedPath("", requestQueryParameters);

        final HttpRequest httpRequest = new HttpRequest("GET", encodedPath, headers.toArray(new HttpHeader[0]), null);

        final S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                .withMetaRequestType(S3MetaRequestOptions.MetaRequestType.DEFAULT).withHttpRequest(httpRequest)
                .withResponseHandler(responseHandler);

        try (final S3MetaRequest metaRequest = s3Client.makeMetaRequest(metaRequestOptions)) {
            addCancelCheckToFuture(resultFuture, metaRequest);
            return resultFuture;
        }
    }

    private ListObjectsV2Output parseListObjectsV2Output(final InputStream stream) throws Exception {
        final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        final ListObjectsV2OutputParserHandler parseHandler = new ListObjectsV2OutputParserHandler();
        parser.parse(stream, parseHandler);
        return parseHandler.getOutput();
    }

    @Override
    public void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }

    protected void populateGetObjectRequestHeaders(final Consumer<HttpHeader> headerConsumer,
            final GetObjectRequest request) {
        // https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html
        if (request.ifMatch() != null) {
            headerConsumer.accept(new HttpHeader("If-Match", request.ifMatch()));
        }
        if (request.ifModifiedSince() != null) {
            headerConsumer.accept(new HttpHeader("If-Modified-Since",
                    DateTimeFormatter.RFC_1123_DATE_TIME.format(request.ifModifiedSince())));
        }
        if (request.ifNoneMatch() != null) {
            headerConsumer.accept(new HttpHeader("If-None-Match", request.ifNoneMatch()));
        }
        if (request.ifUnmodifiedSince() != null) {
            headerConsumer.accept(new HttpHeader("If-Unmodified-Since",
                    DateTimeFormatter.RFC_1123_DATE_TIME.format(request.ifUnmodifiedSince())));
        }
        if (request.range() != null) {
            headerConsumer.accept(new HttpHeader("Range", request.range()));
        }
        if (request.sSECustomerAlgorithm() != null) {
            headerConsumer.accept(
                    new HttpHeader("x-amz-server-side-encryption-customer-algorithm", request.sSECustomerAlgorithm()));
        }
        if (request.sSECustomerKey() != null) {
            headerConsumer
                    .accept(new HttpHeader("x-amz-server-side-encryption-customer-key", request.sSECustomerKey()));
        }
        if (request.sSECustomerKeyMD5() != null) {
            headerConsumer.accept(
                    new HttpHeader("x-amz-server-side-encryption-customer-key-MD5", request.sSECustomerKeyMD5()));
        }
        if (request.requestPayer() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-request-payer", request.requestPayer().name()));
        }
        if (request.expectedBucketOwner() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-expected-bucket-owner", request.expectedBucketOwner()));
        }
        // in progress, but shape of method left here
    }

    protected void populateGetObjectOutputHeader(final GetObjectOutput.Builder builder, final HttpHeader header) {
        // https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html
        if ("x-amz-id-2".equalsIgnoreCase(header.getName())) {
            // TODO: customers want this for tracing availability issues
        } else if ("x-amz-request-id".equalsIgnoreCase(header.getName())) {
            // TODO: customers want this for tracing availability issues
        } else if ("Last-Modified".equalsIgnoreCase(header.getName())) {
            builder.lastModified(DateTimeFormatter.RFC_1123_DATE_TIME.parse(header.getValue(), Instant::from));
        } else if ("ETag".equalsIgnoreCase(header.getName())) {
            builder.eTag(header.getValue());
        } else if ("x-amz-version-id".equalsIgnoreCase(header.getName())) {
            builder.versionId(header.getValue());
        } else if ("Accept-Ranges".equalsIgnoreCase(header.getName())) {
            builder.acceptRanges(header.getValue());
        } else if ("Content-Type".equalsIgnoreCase(header.getName())) {
            builder.contentType(header.getValue());
        } else if ("Content-Length".equalsIgnoreCase(header.getName())) {
            builder.contentLength(Long.parseLong(header.getValue()));
        } else {
            // unhandled header is not necessarily bad, but potentially logged
        }
    }

    protected void populatePutObjectRequestHeaders(final Consumer<HttpHeader> headerConsumer,
            final PutObjectRequest request) {
        // https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html
        if (request.aCL() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-acl", request.aCL().name()));
        }
        if (request.cacheControl() != null) {
            headerConsumer.accept(new HttpHeader("Cache-Control", request.cacheControl()));
        }
        if (request.contentDisposition() != null) {
            headerConsumer.accept(new HttpHeader("Content-Disposition", request.contentDisposition()));
        }
        if (request.contentEncoding() != null) {
            headerConsumer.accept(new HttpHeader("Content-Encoding", request.contentEncoding()));
        }
        if (request.contentLanguage() != null) {
            headerConsumer.accept(new HttpHeader("Content-Language", request.contentLanguage()));
        }
        if (request.contentLength() != null) {
            headerConsumer.accept(new HttpHeader("Content-Length", Long.toString(request.contentLength())));
        }
        if (request.contentMD5() != null) {
            headerConsumer.accept(new HttpHeader("Content-MD5", request.contentMD5()));
        }
        if (request.contentType() != null) {
            headerConsumer.accept(new HttpHeader("Content-Type", request.contentType()));
        }
        if (request.expires() != null) {
            headerConsumer
                    .accept(new HttpHeader("Expires", DateTimeFormatter.RFC_1123_DATE_TIME.format(request.expires())));
        }
        if (request.grantFullControl() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-grant-full-control", request.grantFullControl()));
        }
        if (request.grantRead() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-grant-read", request.grantRead()));
        }
        if (request.grantReadACP() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-grant-read-acp", request.grantReadACP()));
        }
        if (request.grantWriteACP() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-grant-write-acp", request.grantWriteACP()));
        }
        if (request.serverSideEncryption() != null) {
            headerConsumer
                    .accept(new HttpHeader("x-amz-server-side-encryption", request.serverSideEncryption().name()));
        }
        if (request.storageClass() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-storage-class", request.storageClass().name()));
        }
        if (request.websiteRedirectLocation() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-website-redirect-location", request.websiteRedirectLocation()));
        }
        if (request.sSECustomerAlgorithm() != null) {
            headerConsumer.accept(
                    new HttpHeader("x-amz-server-side-encryption-customer-algorithm", request.sSECustomerAlgorithm()));
        }
        if (request.sSECustomerKey() != null) {
            headerConsumer
                    .accept(new HttpHeader("x-amz-server-side-encryption-customer-key", request.sSECustomerKey()));
        }
        if (request.sSECustomerKeyMD5() != null) {
            headerConsumer.accept(
                    new HttpHeader("x-amz-server-side-encryption-customer-key-MD5", request.sSECustomerKeyMD5()));
        }
        if (request.sSEKMSKeyId() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-server-side-encryption-aws-kms-key-id", request.sSEKMSKeyId()));
        }
        if (request.sSEKMSEncryptionContext() != null) {
            headerConsumer
                    .accept(new HttpHeader("x-amz-server-side-encryption-context", request.sSEKMSEncryptionContext()));
        }
        if (request.bucketKeyEnabled() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-server-side-encryption-bucket-key-enabled",
                    Boolean.toString(request.bucketKeyEnabled())));
        }
        if (request.requestPayer() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-request-payer", request.requestPayer().name()));
        }
        if (request.tagging() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-tagging", request.tagging()));
        }
        if (request.objectLockMode() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-object-lock-mode", request.objectLockMode().name()));
        }
        if (request.objectLockRetainUntilDate() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-object-lock-retain-until-date",
                    DateTimeFormatter.RFC_1123_DATE_TIME.format(request.objectLockRetainUntilDate())));
        }
        if (request.objectLockLegalHoldStatus() != null) {
            headerConsumer
                    .accept(new HttpHeader("x-amz-object-lock-legal-hold", request.objectLockLegalHoldStatus().name()));
        }
        if (request.expectedBucketOwner() != null) {
            headerConsumer.accept(new HttpHeader("x-amz-expected-bucket-owner", request.expectedBucketOwner()));
        }
    }

    protected void populatePutObjectOutputHeader(final PutObjectOutput.Builder builder, final HttpHeader header) {
        // https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html
        if ("xamz-id-2".equalsIgnoreCase(header.getName())) {
            // TODO: customers want this for tracing availability issues
        } else if ("x-amz-request-id".equalsIgnoreCase(header.getName())) {
            // TODO: customers want this for tracing availability issues
        } else if ("x-amz-version-id".equalsIgnoreCase(header.getName())) {
            builder.versionId(header.getValue());
        } else if ("ETag".equalsIgnoreCase(header.getName())) {
            builder.eTag(header.getValue());
        } else if ("x-amz-expiration".equalsIgnoreCase(header.getName())) {
            builder.expiration(header.getValue());
        } else if ("x-amz-server-side-encryption".equalsIgnoreCase(header.getName())) {
            builder.serverSideEncryption(ServerSideEncryption.fromValue(header.getValue()));
        } else if ("x-amz-server-side-encryption-aws-kms-key-id".equalsIgnoreCase(header.getName())) {
            builder.sSEKMSKeyId(header.getValue());
        } else if ("x-amz-server-side-encryption-bucket-key-enabled".equalsIgnoreCase(header.getName())) {
            builder.bucketKeyEnabled(Boolean.parseBoolean(header.getValue())); // need verification
        } else if ("x-amz-request-charged".equalsIgnoreCase(header.getName())) {
            builder.requestCharged(RequestCharged.fromValue(header.getValue()));
        }
    }
}
