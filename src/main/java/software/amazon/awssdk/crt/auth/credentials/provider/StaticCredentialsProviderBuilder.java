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
package software.amazon.awssdk.crt.auth.credentials.provider;

import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;

public class StaticCredentialsProviderBuilder {

    private String accessKeyId;
    private String secretAccessKey;
    private String sessionToken;

    public StaticCredentialsProviderBuilder() {}

    public StaticCredentialsProviderBuilder withAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;

        return this;
    }

    public StaticCredentialsProviderBuilder withSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;

        return this;
    }

    public StaticCredentialsProviderBuilder withSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;

        return this;
    }

    public StaticCredentialsProviderBuilder withCredentials(Credentials credentials) {
        this.accessKeyId = credentials.getAccessKeyId();
        this.secretAccessKey = credentials.getSecretAccessKey();
        this.sessionToken = credentials.getSessionToken();

        return this;
    }

    public CredentialsProvider build() {
        long providerHandle = staticCredentialsProviderNew(accessKeyId, secretAccessKey, sessionToken);

        if (providerHandle == 0) {
            return null;
        }

        return new CredentialsProvider(providerHandle);
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long staticCredentialsProviderNew(String accessKeyId, String secretAccessKey, String sessionToken);
}
