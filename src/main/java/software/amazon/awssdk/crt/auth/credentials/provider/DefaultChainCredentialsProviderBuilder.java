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
import software.amazon.awssdk.crt.io.ClientBootstrap;

public class DefaultChainCredentialsProviderBuilder {

    private ClientBootstrap clientBootstrap;

    public DefaultChainCredentialsProviderBuilder() {}

    public DefaultChainCredentialsProviderBuilder withClientBoostrap(ClientBootstrap clientBootstrap) {
        this.clientBootstrap = clientBootstrap;

        return this;
    }

    public CredentialsProvider build() {
        long providerHandle = defaultChainCredentialsProviderNew(clientBootstrap.native_ptr());

        if (providerHandle == 0) {
            return null;
        }

        CredentialsProvider provider = new CredentialsProvider(providerHandle);
        if (provider != null) {
            provider.addReferenceTo(clientBootstrap);
        }

        return provider;
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long defaultChainCredentialsProviderNew(long bootstrapHandle);
}
