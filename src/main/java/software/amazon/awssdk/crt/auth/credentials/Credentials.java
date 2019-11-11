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
