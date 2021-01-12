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
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.s3.*;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

public class S3NativeClient implements  AutoCloseable {
    final EventLoopGroup elGroup;
    final ClientBootstrap clientBootstrap;
    final String signingRegion;
    final CredentialsProvider credentialsProvider;
    final S3Client s3Client;

    public S3NativeClient(final EventLoopGroup elGroup, 
                          final ClientBootstrap clientBootstrap,
                          final String signingRegion,
                          final CredentialsProvider credentialsProvider,
                          final long partSizeBytes,
                          final double targetThroughputGbps) {
        this.elGroup = elGroup;
        this.clientBootstrap = clientBootstrap;
        this.signingRegion = signingRegion;
        this.credentialsProvider = credentialsProvider;

        final S3ClientOptions clientOptions = new S3ClientOptions()
                .withClientBootstrap(clientBootstrap)
                .withCredentialsProvider(credentialsProvider)
                .withRegion(signingRegion)
                .withPartSize(partSizeBytes)
                .withThroughputTargetGbps(targetThroughputGbps);
        
        s3Client = new S3Client(clientOptions); //lazy init this on the first request
    }
    
    public GetObjectOutput getObject(GetObjectRequest request, final ResponseDataConsumer dataHandler) {
        final CompletableFuture<Void> transferComplete = new CompletableFuture<>();
        final S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
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
                        transferComplete.completeExceptionally(ex);
                    }
                    else {
                        transferComplete.complete(null);
                    }
                }
            }
        };

        List<HttpHeader> headers = new LinkedList<>();
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
            transferComplete.get();
            return GetObjectOutput.builder().build();   //TODO: empty response for now
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    public PutObjectOutput putObject(PutObjectRequest request, final RequestDataSupplier requestDataSupplier) {
        final CompletableFuture<Void> transferComplete = new CompletableFuture<>();
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
                    //TODO: this should represent a cancellation of sorts
                    transferComplete.completeExceptionally(e);
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
        headers.add(new HttpHeader("Host", request.bucket() + ".s3." + signingRegion + ".amazonaws.com"));
        headers.add(new HttpHeader("Content-Length", Long.toString(request.contentLength())));
        final StringBuilder keyString = new StringBuilder("/" + request.key());
        HttpRequest httpRequest = new HttpRequest("PUT", keyString.toString(), headers.toArray(new HttpHeader[0]), payloadStream);

        final S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
            @Override
            public int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                return 0;
            }

            @Override
            public void onFinished(int errorCode) {
                if (errorCode == CRT.AWS_CRT_SUCCESS) {
                    transferComplete.complete(null);
                } else {
                    transferComplete.completeExceptionally(new CrtRuntimeException(errorCode, CRT.awsErrorString(errorCode)));
                }
            }
        };
        S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                .withMetaRequestType(S3MetaRequestOptions.MetaRequestType.PUT_OBJECT)
                .withHttpRequest(httpRequest)
                .withResponseHandler(responseHandler);
        
        try (final S3MetaRequest metaRequest = s3Client.makeMetaRequest(metaRequestOptions)) {
            transferComplete.get();
            return PutObjectOutput.builder().build();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }
}
