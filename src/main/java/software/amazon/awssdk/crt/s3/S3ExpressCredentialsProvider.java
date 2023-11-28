/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.auth.credentials.Credentials;

/**
 * The Java object for Native code to invoke.
 */
public class S3ExpressCredentialsProvider {

    private S3ExpressCredentialsProviderHandler handler;

    public S3ExpressCredentialsProvider(S3ExpressCredentialsProviderHandler handler) {
        this.handler = handler;
    }

    public void getS3ExpressCredentials(S3ExpressCredentialsProperties properties, Credentials origCredentials, long nativeHandler) {
        handler.getS3ExpressCredentials(properties, origCredentials).whenComplete((result, ex) -> {
            if(ex != null) {
                s3expressCredentialsProviderGetCredentialsCompleted(nativeHandler, null);
            } else {
                s3expressCredentialsProviderGetCredentialsCompleted(nativeHandler, result);
            }
        });
    }

    public void destroyProvider() throws Exception {
        /* Block until handler finishes shutdown. It doesn't matter to wait for shutdown */
        this.handler.destroyProvider().get();
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void s3expressCredentialsProviderGetCredentialsCompleted(long nativeHandler, Credentials credentials);
}
