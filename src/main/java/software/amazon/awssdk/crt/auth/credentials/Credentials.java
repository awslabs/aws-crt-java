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

    /**
     * Default constructor
     */
    public Credentials() {}

    /**
     * Constructor
     * @param accessKeyId - access key id to use
     * @param secretAccessKey - secret access key to use
     * @param sessionToken - (optional) session token to use
     */
    public Credentials(byte[] accessKeyId, byte[] secretAccessKey, byte[] sessionToken) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
    }

    /**
     * gets the access key id of the credentials
     */
    public byte[] getAccessKeyId() { return accessKeyId; }

    /**
     * gets the secret access key of the credentials
     */
    public byte[] getSecretAccessKey() { return secretAccessKey; }

    /**
     * gets the session token of the credentials
     */
    public byte[] getSessionToken() { return sessionToken; }
}
