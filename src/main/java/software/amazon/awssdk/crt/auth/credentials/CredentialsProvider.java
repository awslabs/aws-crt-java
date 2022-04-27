/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.auth.credentials;

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CleanableCrtResource;
import software.amazon.awssdk.crt.Log;

/**
 * A base class that represents a source of AWS credentials
 */
public class CredentialsProvider extends CleanableCrtResource {

    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    /**
     * Default constructor
     */
    protected CredentialsProvider() {}

    /**
     * Request credentials from the provider
     * @return A Future for Credentials that will be completed when they are acquired.
     */
    public CompletableFuture<Credentials> getCredentials() {
        CompletableFuture<Credentials> future = new CompletableFuture<>();
        try {
            credentialsProviderGetCredentials(getNativeHandle(), future);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * returns the future that completes when all of this object's native resources have shut down or released
     * properly.
     * @return
     */
    public CompletableFuture<Void> getShutdownCompleteFuture() { return shutdownComplete; }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    protected static native void credentialsProviderRelease(long nativeHandle);
    private static native void credentialsProviderGetCredentials(long nativeHandle, CompletableFuture<Credentials> future);
}
