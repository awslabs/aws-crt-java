/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.auth.credentials;

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

        /**
         * Default constructor
         */
        public DefaultChainCredentialsProviderBuilder() {}

        /**
         * Sets what client bootstrap to use when establishing network connections for credentials sourcing
         * @param clientBootstrap client bootstrap to use for network connection establishment
         * @return this builder object
         */
        public DefaultChainCredentialsProviderBuilder withClientBootstrap(ClientBootstrap clientBootstrap) {
            this.clientBootstrap = clientBootstrap;

            return this;
        }

        ClientBootstrap getClientBootstrap() { return clientBootstrap; }

        /**
         * Creates a new default credentials chain provider based on the builder's configuration
         * @return a new default credentials chain provider based on the builder's configuration
         */
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
