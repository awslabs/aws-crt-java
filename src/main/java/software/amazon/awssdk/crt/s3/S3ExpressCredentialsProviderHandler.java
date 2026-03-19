/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.util.concurrent.CompletableFuture;

import software.amazon.awssdk.crt.auth.credentials.Credentials;

/**
 * Interface to override the S3Express Credentials provider.
 */
public interface S3ExpressCredentialsProviderHandler {
    /**
     * To resolve the S3Express Credentials. Invoked when a single request needs to be signed.
     *
     * @param properties The properties needed to derive the S3Express credentials from.
     * @param origCredentials The original Credentials for fetching S3Express credentials.
     * @return The future to be resolved when the S3 Express credentials are resolved.
     */
    public CompletableFuture<Credentials> getS3ExpressCredentials(S3ExpressCredentialsProperties properties, Credentials origCredentials);

    /**
     * Invoked when the S3 client starts to destroy to clean up related resource.
     *
     * @return The future to be resolved when the resource finishes cleaning up.
     */
    public CompletableFuture<Void> destroyProvider();
}
