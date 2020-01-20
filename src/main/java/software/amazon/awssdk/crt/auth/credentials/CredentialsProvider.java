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

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CrtResource;

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
            credentialsProviderGetCredentials(future, getNativeHandle());
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

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    protected static native void credentialsProviderDestroy(long nativeHandle);
    private static native void credentialsProviderGetCredentials(CompletableFuture<Credentials> future, long nativeHandle);
}
