/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.auth.signing;

import java.util.concurrent.CompletableFuture;
import java.util.List;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.http.HttpHeader;

/**
 * Static class for a variety of AWS signing APIs.
 */
public class AwsSigner {

    /**
     * Signs an http request according to the supplied signing configuration
     * 
     * @param request http request to sign
     * @param config  signing configuration
     * @return future which will contain the signed request
     */
    static public CompletableFuture<HttpRequest> signRequest(HttpRequest request, AwsSigningConfig config) {
        CompletableFuture<HttpRequest> future = new CompletableFuture<HttpRequest>();

        CompletableFuture<AwsSigningResult> result = sign(request, config);
        result.whenComplete((res, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
            } else {
                future.complete(res.getSignedRequest());
            }
        });

        return future;
    }

    /**
     * Signs a body chunk according to the supplied signing configuration
     * 
     * @param chunkBody         stream of bytes that make up the chunk
     * @param previousSignature the signature of the previous component of the
     *                          request: either the request itself for the first
     *                          chunk, or the previous chunk otherwise
     * @param config            signing configuration
     * @return future which will contain the signature of the chunk. The signature
     *         *MUST* be written directly into the chunk metadata.
     */
    static public CompletableFuture<byte[]> signChunk(HttpRequestBodyStream chunkBody, byte[] previousSignature,
            AwsSigningConfig config) {
        CompletableFuture<byte[]> future = new CompletableFuture<byte[]>();

        CompletableFuture<AwsSigningResult> result = sign(chunkBody, previousSignature, config);
        result.whenComplete((res, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
            } else {
                future.complete(res.getSignature());
            }
        });

        return future;
    }

    /**
     * Signs an http request according to the supplied signing configuration
     * 
     * @param request http request to sign
     * @param config  signing configuration
     * @return future which will contain a signing result, which provides easier
     *         access to all signing-related result properties
     */
    static public CompletableFuture<AwsSigningResult> sign(HttpRequest request, AwsSigningConfig config) {
        CompletableFuture<AwsSigningResult> future = new CompletableFuture<AwsSigningResult>();

        try {
            awsSignerSignRequest(request, request.marshalForJni(), config, future);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Signs a body chunk according to the supplied signing configuration
     * 
     * @param chunkBody         stream of bytes that make up the chunk
     * @param previousSignature the signature of the previous component of the
     *                          request: either the request itself for the first
     *                          chunk, or the previous chunk otherwise
     * @param config            signing configuration
     * @return future which will contain a signing result, which provides easier
     *         access to all signing-related result properties
     */
    static public CompletableFuture<AwsSigningResult> sign(HttpRequestBodyStream chunkBody, byte[] previousSignature,
            AwsSigningConfig config) {
        CompletableFuture<AwsSigningResult> future = new CompletableFuture<AwsSigningResult>();

        try {
            awsSignerSignChunk(chunkBody, previousSignature, config, future);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Signs a body chunk according to the supplied signing configuration
     * 
     * @param chunkBody         stream of bytes that make up the chunk
     * @param previousSignature the signature of the previous component of the
     *                          request: either the request itself for the first
     *                          chunk, or the previous chunk otherwise
     * @param config            signing configuration
     * @return future which will contain a signing result, which provides easier
     *         access to all signing-related result properties
     */
    static public CompletableFuture<AwsSigningResult> sign(List<HttpHeader> headers, byte[] previousSignature,
            AwsSigningConfig config) {
        CompletableFuture<AwsSigningResult> future = new CompletableFuture<AwsSigningResult>();

        try {
            awsSignerSignTrailingHeaders(HttpHeader.marshalHeadersForJni(headers), previousSignature, config, future);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void awsSignerSignRequest(HttpRequest request, byte[] marshalledRequest,
            AwsSigningConfig config, CompletableFuture<AwsSigningResult> future) throws CrtRuntimeException;

    private static native void awsSignerSignChunk(HttpRequestBodyStream chunk, byte[] previousSignature,
            AwsSigningConfig config, CompletableFuture<AwsSigningResult> future) throws CrtRuntimeException;

    private static native void awsSignerSignTrailingHeaders(byte[] headers, byte[] previousSignature,
            AwsSigningConfig config, CompletableFuture<AwsSigningResult> future) throws CrtRuntimeException;
}
