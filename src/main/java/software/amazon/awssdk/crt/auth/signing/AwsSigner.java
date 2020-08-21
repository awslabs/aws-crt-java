/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.auth.signing;

import java.util.concurrent.CompletableFuture;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;

public class AwsSigner {

    static public CompletableFuture<HttpRequest> signRequest(HttpRequest request, AwsSigningConfig config) {
        CompletableFuture<HttpRequest> future = new CompletableFuture<HttpRequest>();

        try {
            awsSignerSignRequest(request, request.marshalForJni(), config, future);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void awsSignerSignRequest(
        HttpRequest request,
        byte[] marshalledRequest,
        AwsSigningConfig config,
        CompletableFuture<HttpRequest> future) throws CrtRuntimeException;
}
