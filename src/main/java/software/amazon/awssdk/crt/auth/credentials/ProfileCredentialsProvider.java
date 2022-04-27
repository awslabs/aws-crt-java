/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.auth.credentials;

import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.TlsContext;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * A class that wraps a provider that gets credentials from profile files.
 */
public class ProfileCredentialsProvider extends CredentialsProvider {
    /**
     * Create a profile credentials provider using the default file locations and profile name.
     * @return A profile credentials provider.
     */
    public static ProfileCredentialsProvider create() {
        return builder().build();
    }

    /**
     * Get a builder for creating a custom profile credentials provider.
     * @return A builder.
     */
    public static Builder builder() {
        return new BuilderImpl();
    }

    private static byte[] toByteArray(String string) {
        return string == null ? null : string.getBytes(StandardCharsets.UTF_8);
    }

    private ProfileCredentialsProvider(BuilderImpl builder) {
        super();

        try (ClientBootstrap bootstrap = builder.clientBootstrap == null
                ? new ClientBootstrap(null, null)
                : builder.clientBootstrap) {

            long nativeHandle = profileCredentialsProviderNew(
                    bootstrap.getNativeHandle(),
                    builder.tlsContext != null ? builder.tlsContext.getNativeHandle() : 0,
                    toByteArray(builder.profileName),
                    toByteArray(builder.configFileNameOverride),
                    toByteArray(builder.credentialsFileNameOverride),
                    getShutdownCompleteFuture()
            );

            acquireNativeHandle(nativeHandle, CredentialsProvider::credentialsProviderRelease);
        }
    }

    /**
     * A builder for creating a custom profile credentials provider.
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
         * Sets the name of the profile to use. If none is specified, the profile named "default" is used.
         * @param profileName the profile name to use
         * @return The current builder
         */
        Builder withProfileName(String profileName);

        /**
         * Sets the name of the config file to use. If none is specified, a name of "~/.aws/config" (Linux and Mac) or
         * "%USERPROFILE%\.aws\config" (Windows) is used.
         * @param configFileNameOverride the config file name to use
         * @return The current builder
         */
        Builder withConfigFileNameOverride(String configFileNameOverride);

        /**
         * Sets the name of the credentials file to use. If none is specified, a name of "~/.aws/credentials" (Linux and
         * Mac) or "%USERPROFILE%\.aws\credentials" (Windows) is used.
         * @param credentialsFileNameOverride the credentials file name to use
         * @return The current builder
         */
        Builder withCredentialsFileNameOverride(String credentialsFileNameOverride);

        /**
         * Create a profile credentials provider using the configuration applied to this builder.
         * @return A new profile credentials provider.
         */
        ProfileCredentialsProvider build();
    }

    static final class BuilderImpl implements Builder {
        private ClientBootstrap clientBootstrap;
        private TlsContext tlsContext;
        private String profileName;
        private String configFileNameOverride;
        private String credentialsFileNameOverride;

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
        public Builder withProfileName(String profileName) {
            this.profileName = profileName;
            return this;
        }

        @Override
        public Builder withConfigFileNameOverride(String configFileNameOverride) {
            this.configFileNameOverride = configFileNameOverride;
            return this;
        }

        @Override
        public Builder withCredentialsFileNameOverride(String credentialsFileNameOverride) {
            this.credentialsFileNameOverride = credentialsFileNameOverride;
            return this;
        }

        @Override
        public ProfileCredentialsProvider build() {
            return new ProfileCredentialsProvider(this);
        }
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long profileCredentialsProviderNew(
            long bootstrapHandle,
            long tlsContextHandle,
            byte[] profileNameOverride,
            byte[] configFileNameOverride,
            byte[] credentialsFileNameOverride,
            CompletableFuture<Void> shutdownCompleteCallback);
}
