/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.auth.credentials;

import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.TlsContext;


/**
 * A class that wraps a credentials provider that sources session credentials from the AWS Cognito Identity service.
 */
public class CognitoCredentialsProvider extends CredentialsProvider {

    private final static Charset UTF8 = java.nio.charset.StandardCharsets.UTF_8;
    private final static int BUFFER_INT_SIZE = 4;

    /**
     * Pair of strings specifying an identity provider name and an associated login token.
     */
    static public class CognitoLoginTokenPair {

        public final byte[] identityProviderName;
        public final byte[] identityProviderToken;

        public CognitoLoginTokenPair(String identityProviderName, String identityProviderToken) {
            this.identityProviderName = identityProviderName.getBytes(UTF8);
            this.identityProviderToken = identityProviderToken.getBytes(UTF8);
        }
    };

    /**
     * A builder class for the Cognito provider and its options
     */
    static public class CognitoCredentialsProviderBuilder {

        private String endpoint;
        private String identity;
        private String customRoleArn;
        private ArrayList<CognitoLoginTokenPair> logins = new ArrayList<CognitoLoginTokenPair>();

        private TlsContext tlsContext;
        private ClientBootstrap clientBootstrap;
        private HttpProxyOptions httpProxyOptions;

        /**
         * Default constructor
         */
        public CognitoCredentialsProviderBuilder() {}

        /**
         * Sets the Cognito service endpoint to use when sourcing credentials via HTTP
         * @param endpoint cognito service endpoint to use
         * @return The current builder
         */
        public CognitoCredentialsProviderBuilder withEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public String getEndpoint() { return endpoint; }

        /**
         * Sets the Cognito identity to source credentials for
         * @param identity the cognito identity to source credentials for
         * @return The current builder
         */
        public CognitoCredentialsProviderBuilder withIdentity(String identity) {
            this.identity = identity;
            return this;
        }

        public String getIdentity() { return identity; }

        /**
         * (optional) Sets the ARN of the role to be assumed when multiple roles were received in the token from the
         * identity provider.
         * @param customRoleArn ARN of the role to be assumed when multiple roles were received in the token from the
         * identity provider
         * @return The current builder
         */
        public CognitoCredentialsProviderBuilder withCustomRoleArn(String customRoleArn) {
            this.customRoleArn = customRoleArn;
            return this;
        }

        public String getCustomRoleArn() { return customRoleArn; }

        /**
         * Adds an identity provider token pair to allow for authenticated identity access.
         * @param login identity provider token pair
         * @return The current builder
         */
        public CognitoCredentialsProviderBuilder withLogin(CognitoLoginTokenPair login) {
            this.logins.add(login);
            return this;
        }

        public ArrayList<CognitoLoginTokenPair> getLogins() { return logins; }

        /**
         * (Optional) Sets the client bootstrap (host resolver and event loop group) to use when making the connections
         * required by this provider.
         * @param clientBootstrap client bootstrap to use
         * @return The current builder
         */
        public CognitoCredentialsProviderBuilder withClientBootstrap(ClientBootstrap clientBootstrap) {
            this.clientBootstrap = clientBootstrap;

            return this;
        }

        ClientBootstrap getClientBootstrap() { return clientBootstrap; }

        /**
         * Sets the tls context to use when making HTTP requests to the Cognito Identity service
         * @param tlsContext the tls context to use when making HTTP requests
         * @return The current builder
         */
        public CognitoCredentialsProviderBuilder withTlsContext(TlsContext tlsContext) {
            this.tlsContext = tlsContext;

            return this;
        }

        TlsContext getTlsContext() { return tlsContext; }

        /**
         * Sets the proxy configuration to use when making the http request that fetches session
         * credentials from the AWS Cognito Identity service
         * @param httpProxyOptions proxy configuration for the credentials fetching http request
         * @return The current builder
         */
        public CognitoCredentialsProviderBuilder withHttpProxyOptions(HttpProxyOptions httpProxyOptions) {
            this.httpProxyOptions = httpProxyOptions;

            return this;
        }

        HttpProxyOptions getHttpProxyOptions() { return httpProxyOptions; }


        /**
         * Creates a new Cognito credentials provider, based on this builder's configuration
         * @return a new Cognito credentials provider
         */
        public CognitoCredentialsProvider build() {
            return new CognitoCredentialsProvider(this);
        }
    }

    private CognitoCredentialsProvider(CognitoCredentialsProviderBuilder builder) {
        super();

        String endpoint = builder.getEndpoint();
        String identity = builder.getIdentity();
        if (endpoint == null || identity == null) {
            throw new IllegalArgumentException("CognitoCredentialsProvider - endpoint and identity must not be null");
        }

        ClientBootstrap clientBootstrap = builder.getClientBootstrap();
        if (clientBootstrap == null) {
            clientBootstrap = ClientBootstrap.getOrCreateStaticDefault();
        }

        TlsContext tlsContext = builder.getTlsContext();
        if (clientBootstrap == null || tlsContext == null) {
            throw new IllegalArgumentException("CognitoCredentialsProvider - clientBootstrap and tlsContext must not be null");
        }

        int proxyConnectionType = 0;
        long proxyTlsContextHandle = 0;
        String proxyHost = null;
        int proxyPort = 0;
        int proxyAuthorizationType = 0;
        String proxyAuthorizationUsername = null;
        String proxyAuthorizationPassword = null;
        HttpProxyOptions proxyOptions = builder.getHttpProxyOptions();
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

        long nativeHandle = cognitoCredentialsProviderNew(
                this,
                clientBootstrap.getNativeHandle(),
                tlsContext.getNativeHandle(),
                endpoint,
                identity,
                builder.getCustomRoleArn(),
                marshalLoginsForJni(builder.getLogins()),
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

    private void writeLengthPrefixedBytesSafe(ByteBuffer buffer, byte[] bytes) {
        if (bytes != null) {
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        } else {
            buffer.putInt(0);
        }
    }

    private byte[] marshalLoginsForJni(ArrayList<CognitoLoginTokenPair> logins) {
        int size = 0;

        for (CognitoLoginTokenPair login : logins) {
            size += BUFFER_INT_SIZE * 2;
            if (login.identityProviderName != null) {
                size += login.identityProviderName.length;
            }

            if (login.identityProviderToken != null) {
                size += login.identityProviderToken.length;
            }
        }

        if (size == 0) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        for (CognitoLoginTokenPair login : logins) {
            writeLengthPrefixedBytesSafe(buffer, login.identityProviderName);
            writeLengthPrefixedBytesSafe(buffer, login.identityProviderToken);
        }

        return buffer.array();
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long cognitoCredentialsProviderNew(CognitoCredentialsProvider thisObj,
                                                          long bootstrapHandle,
                                                          long tlsContextHandle,
                                                          String endpoint,
                                                          String identity,
                                                          String customRoleArn,
                                                          byte[] marshalledLogins,
                                                          int proxyConnectionType,
                                                          byte[] proxyHost,
                                                          int proxyPort,
                                                          long proxyTlsContext,
                                                          int proxyAuthorizationType,
                                                          byte[] proxyAuthorizationUsername,
                                                          byte[] proxyAuthorizationPassword);
}
