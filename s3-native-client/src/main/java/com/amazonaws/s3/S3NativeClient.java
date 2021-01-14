package com.amazonaws.s3;

import com.amazonaws.s3.model.GetObjectOutput;
import com.amazonaws.s3.model.GetObjectRequest;
import com.amazonaws.s3.model.PutObjectOutput;
import com.amazonaws.s3.model.PutObjectRequest;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.s3.*;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class S3NativeClient implements  AutoCloseable {
    private final S3Client s3Client;
    private final String signingRegion;
    
    public S3NativeClient(final String signingRegion,
                          final ClientBootstrap clientBootstrap,
                          final CredentialsProvider credentialsProvider,
                          final long partSizeBytes,
                          final double targetThroughputGbps) {
        this.signingRegion = signingRegion;
        final S3ClientOptions clientOptions = new S3ClientOptions()
                .withClientBootstrap(clientBootstrap)
                .withCredentialsProvider(credentialsProvider)
                .withRegion(signingRegion)
                .withPartSize(partSizeBytes)
                .withThroughputTargetGbps(targetThroughputGbps);
        
        s3Client = new S3Client(clientOptions); //lazy init this on the first request
    }
    
    public CompletableFuture<GetObjectOutput> getObject(GetObjectRequest request, final ResponseDataConsumer dataHandler) {
        final CompletableFuture<GetObjectOutput> resultFuture = new CompletableFuture<>();
        final GetObjectOutput.Builder resultBuilder = GetObjectOutput.builder();
        final S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
            @Override
            public void onResponseHeaders(final int statusCode, final HttpHeader[] headers) {
                for (int headerIndex = 0; headerIndex < headers.length; ++headerIndex) {
                    try {
                        populateGetObjectOutputHeader(resultBuilder, headers[headerIndex]);
                    } catch (Exception e) {
                        //TODO: log and ignore probably
                    }
                }
                dataHandler.onResponseHeaders(statusCode, headers);
            }
            
            @Override
            public int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                dataHandler.onResponseData(bodyBytesIn);
                return 0;
            }

            @Override
            public void onFinished(int errorCode) {
                CrtRuntimeException ex = null;
                try {
                    if (errorCode != CRT.AWS_CRT_SUCCESS) {
                         ex = new CrtRuntimeException(errorCode, CRT.awsErrorString(errorCode));
                        dataHandler.onException(ex);
                    } else {
                        dataHandler.onFinished();
                    }
                } catch (Exception e) { /* ignore user callback exception */ 
                } finally {
                    if (ex != null) {
                        resultFuture.completeExceptionally(ex);
                    }
                    else {
                        resultFuture.complete(resultBuilder.build());
                    }
                }
            }
        };

        List<HttpHeader> headers = new LinkedList<>();
        // TODO: additional logic needed for *special* partitions
        headers.add(new HttpHeader("Host", request.bucket() + ".s3."+ signingRegion + ".amazonaws.com"));

        final StringBuilder keyString = new StringBuilder("/" + request.key());
        final Map<String, String> requestParams = new HashMap<>();
        if (request.partNumber() != null) {
            requestParams.put("PartNumber", Integer.toString(request.partNumber()));
        }

        HttpRequest httpRequest = new HttpRequest("GET", keyString.toString(),
                headers.toArray(new HttpHeader[0]), null);

        S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                .withMetaRequestType(S3MetaRequestOptions.MetaRequestType.GET_OBJECT)
                .withHttpRequest(httpRequest)
                .withResponseHandler(responseHandler);

        try (final S3MetaRequest metaRequest = s3Client.makeMetaRequest(metaRequestOptions)) {
            return resultFuture;
        }
    }
    
    public CompletableFuture<PutObjectOutput> putObject(PutObjectRequest request, final RequestDataSupplier requestDataSupplier) {
        final CompletableFuture<PutObjectOutput> resultFuture = new CompletableFuture<>();
        final PutObjectOutput.Builder resultBuilder = PutObjectOutput.builder();
        HttpRequestBodyStream payloadStream = new HttpRequestBodyStream() {
            @Override
            public boolean sendRequestBody(final ByteBuffer outBuffer) {
                try {
                    if (outBuffer.hasArray()) {
                        return requestDataSupplier.getRequestBytes(outBuffer.array());
                    } else {
                        byte[] buffer = new byte[outBuffer.capacity()];
                        boolean returnFlag = requestDataSupplier.getRequestBytes(buffer);
                        outBuffer.put(buffer);  //assert outBuffer.remaining() == 0
                        return returnFlag;
                    }
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
        //TODO: additional logic needed for *special* partitions
        headers.add(new HttpHeader("Host", request.bucket() + ".s3." + signingRegion + ".amazonaws.com"));
        headers.add(new HttpHeader("Content-Length", Long.toString(request.contentLength())));
        final StringBuilder keyString = new StringBuilder("/" + request.key());
        HttpRequest httpRequest = new HttpRequest("PUT", keyString.toString(), headers.toArray(new HttpHeader[0]), payloadStream);

        final S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
            @Override
            public void onResponseHeaders(final int statusCode, final HttpHeader[] headers) {
                requestDataSupplier.onResponseHeaders(statusCode, headers);
            }
            
            @Override
            public int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                return 0;
            }

            @Override
            public void onFinished(int errorCode) {
                if (errorCode == CRT.AWS_CRT_SUCCESS) {
                    resultFuture.complete(resultBuilder.build());
                } else {
                    resultFuture.completeExceptionally(new CrtRuntimeException(errorCode, CRT.awsErrorString(errorCode)));
                }
            }
        };
        S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                .withMetaRequestType(S3MetaRequestOptions.MetaRequestType.PUT_OBJECT)
                .withHttpRequest(httpRequest)
                .withResponseHandler(responseHandler);
        
        try (final S3MetaRequest metaRequest = s3Client.makeMetaRequest(metaRequestOptions)) {
            return resultFuture;
        }
    }

    @Override
    public void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }
    
    protected void populateGetObjectOutputHeader(final GetObjectOutput.Builder builder, final HttpHeader header) {
        if ("x-amz-id-2".equalsIgnoreCase(header.getName())) {
            //TODO: customers want this for tracing availability issues
        } else if ("x-amz-request-id".equalsIgnoreCase(header.getName())) {
            //TODO: customers want this for tracing availability issues
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
            //unhandled header is not necessarily bad, but potentially logged
        }
    }

    protected void populatePutObjectOutputHeader(final PutObjectOutput.Builder builder, final HttpHeader header) {
        if ("xamz-id-2".equalsIgnoreCase(header.getName())) {
            //TODO: customers want this for tracing availability issues
        } else if ("x-amz-request-id".equalsIgnoreCase(header.getName())) {
            //TODO: customers want this for tracing availability issues
        } else if ("x-amz-request-id".equalsIgnoreCase(header.getName())) {
        }
    }
}
