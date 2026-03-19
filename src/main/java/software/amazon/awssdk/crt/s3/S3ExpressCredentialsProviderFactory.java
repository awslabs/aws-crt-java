/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;


public interface S3ExpressCredentialsProviderFactory {
    /**
     * A handler to create a S3ExpressCredentialsProvider for the client to use.
     *
     * Warning:
     * You cannot use the client while creating the provider
     * (You can use the client for fetching credentials)
     * @param client The S3Client creates and owns the provider.
     * @return S3ExpressCredentialsProvider created.
     */
    public S3ExpressCredentialsProvider createS3ExpressCredentialsProvider(S3Client client);
}
