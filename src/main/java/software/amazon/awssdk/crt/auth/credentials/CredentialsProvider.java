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
import java.util.concurrent.ConcurrentLinkedQueue;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.Log;

/**
 *
 */
public class CredentialsProvider extends CrtResource implements ICredentialsProvider {

    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    public CredentialsProvider() {}

    /**
     * Request credentials from the provider
     * @return A Future for Credentials that will be completed when they are acquired.
     */
    public CompletableFuture<Credentials> getCredentials() throws CrtRuntimeException {
        return null;
    }

    /**
     *
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
        Log.log(Log.LogLevel.Trace, Log.LogSubject.AuthCredentialsProvider, "CredentialsProvider.onShutdownComplete");

        releaseReferences();

        this.shutdownComplete.complete(null);
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() { return shutdownComplete; }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void credentialsProviderDestroy(CredentialsProvider thisObj, long ncp);
}
