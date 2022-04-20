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
 * A class that wraps a provider that gets credentials from an ECS service.
 * See https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-iam-roles.html for details.
 */
public class EcsCredentialsProvider extends CredentialsProvider {
    /**
     * Create an ECS credentials provider using the defaults.
     * @return An ECS credentials provider.
     */
    public static EcsCredentialsProvider create() {
        return builder().build();
    }

    /**
     * Get a builder for creating a custom ECS credentials provider.
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

    private EcsCredentialsProvider(BuilderImpl builder) {
        super();

        try (ClientBootstrap bootstrap = builder.clientBootstrap == null
                ? new ClientBootstrap(null, null)
                : builder.clientBootstrap) {

            long nativeHandle = ecsCredentialsProviderNew(
                    this,
                    toNativeHandle(bootstrap),
                    toNativeHandle(builder.tlsContext),
                    toByteArray(builder.host),
                    toByteArray(builder.pathAndQuery),
                    toByteArray(builder.authToken)
            );

            acquireNativeHandle(nativeHandle);
            addReferenceTo(bootstrap);
            if (builder.tlsContext != null) {
                addReferenceTo(builder.tlsContext);
            }

            if (builder.clientBootstrap != null) {
                bootstrap.addRef();
            }
        }
        catch (Exception e) {
            super.close();
            throw e;
        }
    }

    /**
     * A builder for creating a custom ECS credentials provider.
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
         * @param host Host used to connect to the credential service.
         * @return The current builder
         */
        Builder withHost(String host);

        /**
         * @param pathAndQuery The URL path and query used to connect to the credential service.
         * @return The current builder
         */
        Builder withPathAndQuery(String pathAndQuery);

        /**
         * @param authToken Token used to retrieve credentials.
         * @return The current builder
         */
        Builder withAuthToken(String authToken);

        /**
         * Create an ECS credentials provider using the configuration applied to this builder.
         * @return A new ECS credentials provider.
         */
        EcsCredentialsProvider build();
    }

    static final class BuilderImpl implements Builder {
        private ClientBootstrap clientBootstrap;
        private TlsContext tlsContext;
        private String host;
        private String pathAndQuery;
        private String authToken;

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
        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        @Override
        public Builder withPathAndQuery(String pathAndQuery) {
            this.pathAndQuery = pathAndQuery;
            return this;
        }

        @Override
        public Builder withAuthToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        @Override
        public EcsCredentialsProvider build() {
            return new EcsCredentialsProvider(this);
        }
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long ecsCredentialsProviderNew(
            EcsCredentialsProvider thisObj,
            long bootstrapHandle,
            long tlsContextHandle,
            byte[] host,
            byte[] pathAndQuery,
            byte[] authToken);
}
