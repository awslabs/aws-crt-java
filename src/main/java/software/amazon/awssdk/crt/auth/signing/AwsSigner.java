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

import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpRequest;

/**
 * A class representing
 */
public class AwsSigner extends CrtResource {

    public AwsSigner() {
        acquireNativeHandle(awsSignerNew());
    }

    public CompletableFuture<HttpRequest> signRequest(HttpRequest request, AwsSigningConfig config) {
        CompletableFuture<HttpRequest> future = new CompletableFuture<HttpRequest>();

        try {
            awsSignerSignRequest(this, request, config, future, getNativeHandle());
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Begins the release process of the signer's native handle
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            awsSignerDestroy(this, getNativeHandle());
        }
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return false; }


    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long awsSignerNew();
    private static native void awsSignerDestroy(AwsSigner thisObj, long nativeHandle);
    private static native void awsSignerSignRequest(AwsSigner thisObj, HttpRequest request, AwsSigningConfig config, CompletableFuture<HttpRequest> future, long signerNativeHandle) throws CrtRuntimeException;
}
