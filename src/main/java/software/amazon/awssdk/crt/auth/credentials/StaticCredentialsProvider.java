/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.auth.credentials;

import java.lang.IllegalArgumentException;
import java.util.concurrent.CompletableFuture;

/**
 * A class that wraps the a credentials provider that returns a fixed set of credentials
 */
public class StaticCredentialsProvider extends CredentialsProvider {

    /**
     * A simple builder class for a static credentials provider and its options
     */
    static public class StaticCredentialsProviderBuilder {

        private byte[] accessKeyId;
        private byte[] secretAccessKey;
        private byte[] sessionToken;

        /**
         * Default constructor
         */
        public StaticCredentialsProviderBuilder() {}

        /**
         * Sets the AWS access key id to use within the static credentials
         * @param accessKeyId AWS access key id to use
         * @return this builder object
         */
        public StaticCredentialsProviderBuilder withAccessKeyId(byte[] accessKeyId) {
            this.accessKeyId = accessKeyId;

            return this;
        }

        byte[] getAccessKeyId() { return accessKeyId; }

        /**
         * Sets the AWS secret access key to use within the static credentials
         * @param secretAccessKey AWS secret access key to use
         * @return this builder object
         */
        public StaticCredentialsProviderBuilder withSecretAccessKey(byte[] secretAccessKey) {
            this.secretAccessKey = secretAccessKey;

            return this;
        }

        byte[] getSecretAccessKey() { return secretAccessKey; }

        /**
         * Sets the AWS session token to use within the static credentials.  Session credentials are inherently
         * time-bound; static providers do not provide any mechanism to update session-based credentials, and use
         * of session-based credentials with a static provider is discouraged.
         * @param sessionToken AWS session token to use
         * @return this builder object
         */
        public StaticCredentialsProviderBuilder withSessionToken(byte[] sessionToken) {
            this.sessionToken = sessionToken;

            return this;
        }

        byte[] getSessionToken() { return sessionToken; }

        /**
         * sets the entire credential set to use within the static credentials provider.  Overrides all three
         * components.
         * @param credentials AWS credentials to use
         * @return this builder object
         */
        public StaticCredentialsProviderBuilder withCredentials(Credentials credentials) {
            this.accessKeyId = credentials.getAccessKeyId();
            this.secretAccessKey = credentials.getSecretAccessKey();
            this.sessionToken = credentials.getSessionToken();

            return this;
        }

        /**
         * Builds a new static credentials provider based on the builder configuration
         * @return a new static credentials provider
         */
        public StaticCredentialsProvider build() {
            return new StaticCredentialsProvider(this);
        }
    }

    private StaticCredentialsProvider(StaticCredentialsProviderBuilder builder) {
        super();
        byte[] accessKeyId = builder.getAccessKeyId();
        byte[] secretAccessKey = builder.getSecretAccessKey();
        if (accessKeyId == null || secretAccessKey == null) {
            throw new IllegalArgumentException("StaticCredentialsProvider - accessKeyId and secretAccessKey must be non null");
        }

        byte[] sessionToken = builder.getSessionToken();

        long nativeHandle = staticCredentialsProviderNew(accessKeyId, secretAccessKey, sessionToken, getShutdownCompleteFuture());
        acquireNativeHandle(nativeHandle, CredentialsProvider::credentialsProviderRelease);
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long staticCredentialsProviderNew(byte[] accessKeyId, byte[] secretAccessKey, byte[] sessionToken, CompletableFuture<Void> shutdownCompleteCallback);
}
