/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.auth.credentials;

import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.TlsContext;

import java.util.concurrent.CompletableFuture;

/**
 * Sts with web identity credentials provider sources a set of temporary security credentials for users who have been
 * authenticated in a mobile or web application with a web identity provider.
 */
public class StsWebIdentityCredentialsProvider extends CredentialsProvider {
    /**
     * Create an STS web identity credentials provider.
     * @return An STS web identity credentials provider.
     */
    public static StsWebIdentityCredentialsProvider create() {
        return builder().build();
    }

    /**
     * Get a builder for creating an STS web identity credentials provider.
     * @return A builder.
     */
    public static Builder builder() {
        return new BuilderImpl();
    }

    private StsWebIdentityCredentialsProvider(BuilderImpl builder) {
        super();

        try (ClientBootstrap bootstrap = builder.clientBootstrap == null
                ? new ClientBootstrap(null, null)
                : builder.clientBootstrap) {

            long nativeHandle = stsWebIdentityCredentialsProviderNew(
                    bootstrap.getNativeHandle(),
                    builder.tlsContext.getNativeHandle(),
                    getShutdownCompleteFuture()
            );

            acquireNativeHandle(nativeHandle, CredentialsProvider::credentialsProviderRelease);
        }
    }

    /**
     * A builder for creating an STS web identity credentials provider.
     */
    public interface Builder {
        /**
         * Sets the client bootstrap (host resolver and event loop group) to use when making the connections
         * required by this provider. The default is a bootstrap which uses the static default event loop group and host
         * resolver.
         * @param clientBootstrap client bootstrap to use
         * @return The current builder
         */
        Builder withClientBootstrap(ClientBootstrap clientBootstrap);

        /**
         * Sets the tls context to use for any secure network connections made while sourcing credentials.
         * @param tlsContext the tls context to use when establishing network connections
         * @return The current builder
         */
        Builder withTlsContext(TlsContext tlsContext);

        /**
         * Create an STS web identity credentials provider using the configuration applied to this builder.
         * @return A new STS web identity credentials provider.
         */
        StsWebIdentityCredentialsProvider build();
    }

    static final class BuilderImpl implements Builder {
        private ClientBootstrap clientBootstrap;
        private TlsContext tlsContext;

        BuilderImpl() {}

        @Override
        public Builder withClientBootstrap(ClientBootstrap clientBootstrap) {
            this.clientBootstrap = clientBootstrap;
            return this;
        }

        @Override
        public Builder withTlsContext(TlsContext tlsContext) {
            this.tlsContext = tlsContext;
            return this;
        }

        @Override
        public StsWebIdentityCredentialsProvider build() {
            if (this.tlsContext == null) {
                throw new IllegalArgumentException("TlsContext must be provided");
            }
            return new StsWebIdentityCredentialsProvider(this);
        }
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long stsWebIdentityCredentialsProviderNew(
            long bootstrapHandle,
            long tlsContextHandle,
            CompletableFuture<Void> shutdownCompleteCallback);
}
