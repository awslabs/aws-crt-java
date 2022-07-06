/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.auth.credentials;

import java.lang.IllegalArgumentException;
import java.nio.charset.Charset;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.TlsContext;

/**
 * A class that wraps a credentials provider that sources session credentials from IoT's x509 credentials
 * service.
 */
public class X509CredentialsProvider extends CredentialsProvider {

    private final static Charset UTF8 = java.nio.charset.StandardCharsets.UTF_8;

    /**
     * A builder class for the 509 provider and its options
     */
    static public class X509CredentialsProviderBuilder {

        private String thingName;
        private String roleAlias;
        private String endpoint;

        private TlsContext tlsContext;
        private ClientBootstrap clientBootstrap;
        private HttpProxyOptions proxyOptions;

        /**
         * Default constructor
         */
        public X509CredentialsProviderBuilder() {}

        /**
         * Sets the client bootstrap (host resolver and event loop group) to use when making the connections
         * required by this provider.
         * @param clientBootstrap client bootstrap to use
         * @return The current builder
         */
        public X509CredentialsProviderBuilder withClientBootstrap(ClientBootstrap clientBootstrap) {
            this.clientBootstrap = clientBootstrap;

            return this;
        }

        ClientBootstrap getClientBootstrap() { return clientBootstrap; }

        /**
         * Sets the tls context initialized with a x509 certificate and private key suitable for
         * queries against the account's iot credential provider endpoint
         * @param tlsContext the tls context to use when establishing the http connection to iot
         * @return The current builder
         */
        public X509CredentialsProviderBuilder withTlsContext(TlsContext tlsContext) {
            this.tlsContext = tlsContext;

            return this;
        }

        TlsContext getTlsContext() { return tlsContext; }

        /**
         * Sets the iot thing name to fetch credentials by.
         * @param thingName name of the thing to use
         * @return The current builder
         */
        public X509CredentialsProviderBuilder withThingName(String thingName) {
            this.thingName = thingName;

            return this;
        }

        String getThingName() { return thingName; }

        /**
         * Sets the role alias to fetch credentials through
         * @param roleAlias name of the role alias to use
         * @return The current builder
         */
        public X509CredentialsProviderBuilder withRoleAlias(String roleAlias) {
            this.roleAlias = roleAlias;

            return this;
        }

        String getRoleAlias() { return roleAlias; }

        /**
         * Sets the endpoint to fetch credentials from.  This is a per-account value that can be determined
         * via the cli: 'aws iot describe-endpoint --endpoint-type iot:CredentialProvider'
         * @param endpoint credentials provider endpoint
         * @return The current builder
         */
        public X509CredentialsProviderBuilder withEndpoint(String endpoint) {
            this.endpoint = endpoint;

            return this;
        }

        String getEndpoint() { return endpoint; }

        /**
         * Sets the proxy configuration to use when making the http request that fetches session
         * credentials from the IoT x509 credentials provider service
         * @param proxyOptions proxy configuration for the credentials fetching http request
         * @return The current builder
         */
        public X509CredentialsProviderBuilder withProxyOptions(HttpProxyOptions proxyOptions) {
            this.proxyOptions = proxyOptions;

            return this;
        }

        HttpProxyOptions getProxyOptions() { return proxyOptions; }


        /**
         * Creates a new X509 credentials provider, based on this builder's configuration
         * @return a new X509 credentials provider
         */
        public X509CredentialsProvider build() {
            return new X509CredentialsProvider(this);
        }
    }

    private X509CredentialsProvider(X509CredentialsProviderBuilder builder) {
        super();

        String thingName = builder.getThingName();
        String roleAlias = builder.getRoleAlias();
        String endpoint = builder.getEndpoint();
        if (thingName == null || roleAlias == null || endpoint == null) {
            throw new IllegalArgumentException("X509CredentialsProvider - thingName, roleAlias, and endpoint must be non null");
        }

        ClientBootstrap clientBootstrap = builder.getClientBootstrap();
        if (clientBootstrap == null) {
            clientBootstrap = ClientBootstrap.getOrCreateStaticDefault();
        }

        TlsContext tlsContext = builder.getTlsContext();
        if (clientBootstrap == null || tlsContext == null) {
            throw new IllegalArgumentException("X509CredentialsProvider - clientBootstrap and tlsContext must be non null");
        }

        int proxyConnectionType = 0;
        long proxyTlsContextHandle = 0;
        String proxyHost = null;
        int proxyPort = 0;
        int proxyAuthorizationType = 0;
        String proxyAuthorizationUsername = null;
        String proxyAuthorizationPassword = null;
        HttpProxyOptions proxyOptions = builder.getProxyOptions();
        if (proxyOptions != null) {
            proxyConnectionType = proxyOptions.getConnectionType().getValue();
            TlsContext proxyTlsContext = proxyOptions.getTlsContext();
            if (proxyTlsContext != null) {
                proxyTlsContextHandle = proxyTlsContext.getNativeHandle();
            }

            proxyHost = proxyOptions.getHost();
            proxyPort = proxyOptions.getPort();
            proxyAuthorizationType = proxyOptions.getAuthorizationType().getValue();
            proxyAuthorizationUsername = proxyOptions.getAuthorizationUsername();
            proxyAuthorizationPassword = proxyOptions.getAuthorizationPassword();
        }

        long nativeHandle = x509CredentialsProviderNew(
                this,
                clientBootstrap.getNativeHandle(),
                tlsContext.getNativeHandle(),
                thingName.getBytes(UTF8),
                roleAlias.getBytes(UTF8),
                endpoint.getBytes(UTF8),
                proxyConnectionType,
                proxyHost != null ? proxyHost.getBytes(UTF8) : null,
                proxyPort,
                proxyTlsContextHandle,
                proxyAuthorizationType,
                proxyAuthorizationUsername != null ? proxyAuthorizationUsername.getBytes(UTF8) : null,
                proxyAuthorizationPassword != null ? proxyAuthorizationPassword.getBytes(UTF8) : null);

        acquireNativeHandle(nativeHandle);
        addReferenceTo(clientBootstrap);
        addReferenceTo(tlsContext);
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long x509CredentialsProviderNew(X509CredentialsProvider thisObj,
                                                          long bootstrapHandle,
                                                          long tlsContextHandle,
                                                          byte[] thingName,
                                                          byte[] roleAlias,
                                                          byte[] endpoint,
                                                          int proxyConnectionType,
                                                          byte[] proxyHost,
                                                          int proxyPort,
                                                          long proxyTlsContext,
                                                          int proxyAuthorizationType,
                                                          byte[] proxyAuthorizationUsername,
                                                          byte[] proxyAuthorizationPassword);
}
