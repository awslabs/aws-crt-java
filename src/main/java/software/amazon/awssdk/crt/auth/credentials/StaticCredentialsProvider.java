/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.auth.credentials;

import java.lang.IllegalArgumentException;

import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;

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

        public StaticCredentialsProviderBuilder() {}

        public StaticCredentialsProviderBuilder withAccessKeyId(byte[] accessKeyId) {
            this.accessKeyId = accessKeyId;

            return this;
        }

        byte[] getAccessKeyId() { return accessKeyId; }

        public StaticCredentialsProviderBuilder withSecretAccessKey(byte[] secretAccessKey) {
            this.secretAccessKey = secretAccessKey;

            return this;
        }

        byte[] getSecretAccessKey() { return secretAccessKey; }

        public StaticCredentialsProviderBuilder withSessionToken(byte[] sessionToken) {
            this.sessionToken = sessionToken;

            return this;
        }

        byte[] getSessionToken() { return sessionToken; }

        public StaticCredentialsProviderBuilder withCredentials(Credentials credentials) {
            this.accessKeyId = credentials.getAccessKeyId();
            this.secretAccessKey = credentials.getSecretAccessKey();
            this.sessionToken = credentials.getSessionToken();

            return this;
        }

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

        long nativeHandle = staticCredentialsProviderNew(this, accessKeyId, secretAccessKey, sessionToken);
        acquireNativeHandle(nativeHandle);
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long staticCredentialsProviderNew(StaticCredentialsProvider thisObj, byte[] accessKeyId, byte[] secretAccessKey, byte[] sessionToken);
}
