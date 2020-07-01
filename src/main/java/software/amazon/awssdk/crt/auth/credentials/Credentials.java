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

    public Credentials() {}

    public Credentials(byte[] accessKeyId, byte[] secretAccessKey, byte[] sessionToken) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
    }

    public byte[] getAccessKeyId() { return accessKeyId; }
    public byte[] getSecretAccessKey() { return secretAccessKey; }
    public byte[] getSessionToken() { return sessionToken; }
}
