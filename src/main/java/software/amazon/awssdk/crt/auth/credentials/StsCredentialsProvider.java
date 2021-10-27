/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.auth.credentials;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.TlsContext;

import java.nio.charset.StandardCharsets;

/**
 * A class that wraps a provider that gets credentials from Security Token Service (STS).
 */
public class StsCredentialsProvider extends CredentialsProvider {
    /**
     * Create a sts credentials provider.
     * @return A sts credentials provider.
     */
    public static StsCredentialsProvider create() {
        return builder().build();
    }

    /**
     * Get a builder for creating a custom sts credentials provider.
     * @return A builder.
     */
    public static Builder builder() {
        return new BuilderImpl();
    }

    private static byte[] toByteArray(String string) {
        return string == null ? null : string.getBytes(StandardCharsets.UTF_8);
    }

    private static long toNativeHandle(CrtResource crtResource) {
        return crtResource == null ? 0 : crtResource.getNativeHandle();
    }

    private StsCredentialsProvider(BuilderImpl builder) {
        super();

        try (ClientBootstrap bootstrap = builder.clientBootstrap == null
                ? new ClientBootstrap(null, null)
                : builder.clientBootstrap) {

            long nativeHandle = stsCredentialsProviderNew(
                    this,
                    toNativeHandle(bootstrap),
                    toNativeHandle(builder.tlsContext),
                    builder.credsProvider.getNativeHandle(),
                    toByteArray(builder.roleArn),
                    toByteArray(builder.sessionName),
                    builder.durationSeconds
            );

            acquireNativeHandle(nativeHandle);
            addReferenceTo(bootstrap);
            addReferenceTo(builder.tlsContext);
        }
        catch (Exception e) {
            super.close();
            throw e;
        }
    }

    /**
     * A builder for creating a sts credentials provider.
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
         * Sets the underlying Credentials Provider to use for source credentials
         * @param credsProvider the CredentialsProvider for source credentials
         * @return The current builder
         */
        Builder withCredsProvider(CredentialsProvider credsProvider);

        /**
         * @param roleArn the target role's ARN
         * @return The current builder
         */
        Builder withRoleArn(String roleArn);

        /**
         * @param sessionName the name to associate with the session.
         * @return The current builder
         */
        Builder withSessionName(String sessionName);

        /**
         * @param durationSeconds number of seconds from authentication that the session is valid for.
         * @return the current builder
         */
        Builder withDurationSeconds(int durationSeconds);

        /**
         * Create a sts credentials provider using the configuration applied to this builder.
         * @return A new sts credentials provider.
         */
        StsCredentialsProvider build();
    }

    static final class BuilderImpl implements Builder {
        private ClientBootstrap clientBootstrap;
        private TlsContext tlsContext;
        private CredentialsProvider credsProvider;
        private String roleArn;
        private String sessionName;
        private int durationSeconds;

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
        public Builder withCredsProvider(CredentialsProvider credsProvider) {
            this.credsProvider = credsProvider;
            return this;
        }

        @Override
        public Builder withRoleArn(String roleArn) {
            this.roleArn = roleArn;
            return this;
        }

        @Override
        public Builder withSessionName(String sessionName) {
            this.sessionName = sessionName;
            return this;
        }

        @Override
        public Builder withDurationSeconds(int durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        @Override
        public StsCredentialsProvider build() {
            if (this.tlsContext == null) {
                throw new IllegalArgumentException("TlsContext must be provided");
            }
            return new StsCredentialsProvider(this);
        }
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long stsCredentialsProviderNew(
            StsCredentialsProvider thisObj,
            long bootstrapHandle,
            long tlsContextHandle,
            long creds_provider,
            byte[] roleArn,
            byte[] sessionName,
            int durationSeconds);
}
