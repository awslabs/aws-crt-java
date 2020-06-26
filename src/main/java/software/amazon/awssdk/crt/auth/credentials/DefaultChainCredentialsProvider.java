/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.auth.credentials;

import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.io.ClientBootstrap;

/**
 * A class that wraps the default AWS credentials provider chain
 */
public class DefaultChainCredentialsProvider extends CredentialsProvider {

    /**
     * A simple builder class for the default credentials provider chain and its options
     * Does not add reference to CRT resources
     */
    static public class DefaultChainCredentialsProviderBuilder {

        private ClientBootstrap clientBootstrap;

        public DefaultChainCredentialsProviderBuilder() {}

        public DefaultChainCredentialsProviderBuilder withClientBootstrap(ClientBootstrap clientBootstrap) {
            this.clientBootstrap = clientBootstrap;

            return this;
        }

        public ClientBootstrap getClientBootstrap() { return clientBootstrap; }

        public DefaultChainCredentialsProvider build() {
            return new DefaultChainCredentialsProvider(this);
        }
    }

    private DefaultChainCredentialsProvider(DefaultChainCredentialsProviderBuilder builder) {
        super();
        ClientBootstrap clientBootstrap = builder.getClientBootstrap();
        if (clientBootstrap == null) {
            throw new IllegalArgumentException("DefaultChainCredentialsProvider: clientBootstrap must be non-null");
        }

        long nativeHandle = defaultChainCredentialsProviderNew(this, clientBootstrap.getNativeHandle());
        acquireNativeHandle(nativeHandle);
        addReferenceTo(clientBootstrap);
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long defaultChainCredentialsProviderNew(DefaultChainCredentialsProvider provider, long bootstrapHandle);
}
