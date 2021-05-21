package software.amazon.awssdk.crt.auth.credentials;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.TlsContext;

import java.nio.charset.StandardCharsets;

/**
 * A class that wraps a provider that gets credentials from profile files.
 */
public class ProfileCredentialsProvider extends CredentialsProvider {
    /**
     * A builder class for the profile credentials provider.
     */
    static public class ProfileCredentialsProviderBuilder {
        private ClientBootstrap clientBootstrap;
        private TlsContext tlsContext;
        private String profileName;
        private String configFileNameOverride;
        private String credentialsFileNameOverride;

        public ProfileCredentialsProviderBuilder() {}

        /**
         * Sets the client bootstrap (host resolver and event loop group) to use when making the connections
         * required by this provider.
         * @param clientBootstrap client bootstrap to use
         * @return The current builder
         */
        public ProfileCredentialsProviderBuilder withClientBootstrap(ClientBootstrap clientBootstrap) {
            this.clientBootstrap = clientBootstrap;

            return this;
        }

        ClientBootstrap getClientBootstrap() { return clientBootstrap; }

        /**
         * Sets the tls context to use for any secure network connections made while sourcing credentials.
         * @param tlsContext the tls context to use when establishing network connections
         * @return The current builder
         */
        public ProfileCredentialsProviderBuilder withTlsContext(TlsContext tlsContext) {
            this.tlsContext = tlsContext;
            return this;
        }

        TlsContext getTlsContext() { return tlsContext; }

        /**
         * Sets the name of the profile to use. If none is specified, the profile named "default" is used.
         * @param profileName the profile name to use
         * @return The current builder
         */
        public ProfileCredentialsProviderBuilder withProfileName(String profileName) {
            this.profileName = profileName;
            return this;
        }

        String getProfileName() { return profileName; }

        /**
         * Sets the name of the config file to use. If none is specified, a name of "~/.aws/config" (Linux and Mac) or
         * "%USERPROFILE%\.aws\config" (Windows) is used.
         * @param configFileNameOverride the config file name to use
         * @return The current builder
         */
        public ProfileCredentialsProviderBuilder withConfigFileNameOverride(String configFileNameOverride) {
            this.configFileNameOverride = configFileNameOverride;
            return this;
        }

        String getConfigFileNameOverride() { return configFileNameOverride; }

        /**
         * Sets the name of the credentials file to use. If none is specified, a name of "~/.aws/credentials" (Linux and
         * Mac) or "%USERPROFILE%\.aws\credentials" (Windows) is used.
         * @param credentialsFileNameOverride the credentials file name to use
         * @return The current builder
         */
        public ProfileCredentialsProviderBuilder withCredentialsFileNameOverride(String credentialsFileNameOverride) {
            this.credentialsFileNameOverride = credentialsFileNameOverride;
            return this;
        }

        String getCredentialsFileNameOverride() { return credentialsFileNameOverride; }

        public ProfileCredentialsProvider build() { return new ProfileCredentialsProvider(this); }
    }

    private ProfileCredentialsProvider(ProfileCredentialsProviderBuilder builder) {
        super();

        long nativeHandle = profileCredentialsProviderNew(
                this,
                toNativeHandle(builder.clientBootstrap),
                toNativeHandle(builder.tlsContext),
                toByteArray(builder.profileName),
                toByteArray(builder.configFileNameOverride),
                toByteArray(builder.credentialsFileNameOverride)
        );

        acquireNativeHandle(nativeHandle);
        if (builder.clientBootstrap != null) { addReferenceTo(builder.clientBootstrap); }
        if (builder.tlsContext != null) { addReferenceTo(builder.tlsContext); }
    }

    private static byte[] toByteArray(String string) {
        return string == null ? null : string.getBytes(StandardCharsets.UTF_8);
    }

    private static long toNativeHandle(CrtResource crtResource) {
        return crtResource == null ? 0 : crtResource.getNativeHandle();
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long profileCredentialsProviderNew(
            ProfileCredentialsProvider thisObj,
            long bootstrapHandle,
            long tlsContextHandle,
            byte[] profileNameOverride,
            byte[] configFileNameOverride,
            byte[] credentialsFileNameOverride);
}
