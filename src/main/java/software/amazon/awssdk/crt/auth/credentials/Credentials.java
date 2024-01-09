/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.auth.credentials;

/**
 * A class representing a set of AWS credentials.
 */
public class Credentials {

    /* settable from native */
    private byte[] accessKeyId;
    private byte[] secretAccessKey;
    private byte[] sessionToken;
    /* Not using Instant, as Instant requires Android API 29+. The secs since epoch. */
    private long expirationTimePointSecs;

    /**
     * Anonymous Credentials constructor. Use Anonymous Credentials when you want to skip signing.
     * The recommended approach is to use createAnonymousCredentials().
     */
    public Credentials() {}

    /**
     * @param accessKeyId - access key id to use
     * @param secretAccessKey - secret access key to use
     * @param sessionToken - (optional) session token to use
     */
    public Credentials(byte[] accessKeyId, byte[] secretAccessKey, byte[] sessionToken) {
        this(accessKeyId, secretAccessKey, sessionToken, Long.MAX_VALUE);
    }


    /**
     * @param accessKeyId - access key id to use
     * @param secretAccessKey - secret access key to use
     * @param sessionToken - (optional) session token to use
     * @param expirationTimePointSecs - Time when the credentials expires, as secs since epoch.
     */
    public Credentials(byte[] accessKeyId, byte[] secretAccessKey, byte[] sessionToken, long expirationTimePointSecs) {
        if (accessKeyId == null || secretAccessKey == null) {
            throw new IllegalArgumentException("Credentials - accessKeyId and secretAccessKey must be non null");
        }

        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
        this.expirationTimePointSecs = expirationTimePointSecs;
    }


    /**
     * Anonymous Credentials constructor. Use Anonymous Credentials when you want to skip signing.
     * @return Anonymous Credentials
     */
    public static Credentials createAnonymousCredentials(){
        return new Credentials();
    }

    /**
     * @return the access key id of the credentials
     */
    public byte[] getAccessKeyId() { return accessKeyId; }

    /**
     * @return the secret access key of the credentials
     */
    public byte[] getSecretAccessKey() { return secretAccessKey; }

    /**
     * @return the session token of the credentials
     */
    public byte[] getSessionToken() { return sessionToken; }

    /**
     * @return the expiration timepoint as secs since epoch.
     */
    public long getExpirationTimePointSecs() { return expirationTimePointSecs; }
}
