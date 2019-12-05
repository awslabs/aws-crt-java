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
package software.amazon.awssdk.crt.auth.credentials;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.Log;

/**
 * A base class that represents a source of AWS credentials
 */
public class CredentialsProvider extends CrtResource {

    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    public CredentialsProvider() {}

    /**
     * Request credentials from the provider
     * @return A Future for Credentials that will be completed when they are acquired.
     */
    public CompletableFuture<Credentials> getCredentials() {
        CompletableFuture<Credentials> future = new CompletableFuture<>();
        try {
            credentialsProviderGetCredentials(this, future, getNativeHandle());
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Completion callback for credentials fetching.  Alternatively the future could have been completed
     * at the JNI layer.
     * @param future the future that the credentials should be applied to
     * @param credentials the fetched credentials, if successful
     */
    private void onGetCredentialsComplete(CompletableFuture<Credentials> future, Credentials credentials) {
        if (credentials != null) {
            future.complete(credentials);
        } else {
            future.completeExceptionally(new RuntimeException("Failed to get a valid set of credentials"));
        }
    }

    /**
     * Begins the release process of the provider's native handle
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            credentialsProviderDestroy(this, getNativeHandle());
        }
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return false; }

    /**
     * Called from Native when the asynchronous shutdown process needed for credentials providers has completed.
     */
    private void onShutdownComplete() {
        Log.log(Log.LogLevel.Trace, Log.LogSubject.AuthCredentialsProvider, "CrtCredentialsProvider.onShutdownComplete");

        releaseReferences();

        this.shutdownComplete.complete(null);
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() { return shutdownComplete; }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void credentialsProviderDestroy(CredentialsProvider thisObj, long nativeHandle);
    private static native void credentialsProviderGetCredentials(CredentialsProvider thisObj, CompletableFuture<Credentials> future, long nativeHandle);
}
