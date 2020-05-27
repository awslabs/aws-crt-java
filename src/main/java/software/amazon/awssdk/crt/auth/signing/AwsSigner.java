/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
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
