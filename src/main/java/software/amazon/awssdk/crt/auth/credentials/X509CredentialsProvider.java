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
package software.amazon.awssdk.crt.auth.credentials;

import java.lang.IllegalArgumentException;
import java.nio.charset.Charset;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.TlsContext;

/**
 * A class that wraps the a credentials provider that returns a fixed set of credentials
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

        public X509CredentialsProviderBuilder() {}

        /**
         * Client bootstrap (host resolver and event loop group) to use when making the connections
         * required by this provider.
         */
        public X509CredentialsProviderBuilder withClientBootstrap(ClientBootstrap clientBootstrap) {
            this.clientBootstrap = clientBootstrap;

            return this;
        }

        ClientBootstrap getClientBootstrap() { return clientBootstrap; }

        /**
         * Tls context initialized with an x509 certificate and private key
         */
        public X509CredentialsProviderBuilder withTlsContext(TlsContext tlsContext) {
            this.tlsContext = tlsContext;

            return this;
        }

        TlsContext getTlsContext() { return tlsContext; }

        public X509CredentialsProviderBuilder withThingName(String thingName) {
            this.thingName = thingName;

            return this;
        }

        String getThingName() { return thingName; }

        public X509CredentialsProviderBuilder withRoleAlias(String roleAlias) {
            this.roleAlias = roleAlias;

            return this;
        }

        String getRoleAlias() { return roleAlias; }

        public X509CredentialsProviderBuilder withEndpoint(String endpoint) {
            this.endpoint = endpoint;

            return this;
        }

        String getEndpoint() { return endpoint; }

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
        TlsContext tlsContext = builder.getTlsContext();
        if (clientBootstrap == null || tlsContext == null) {
            throw new IllegalArgumentException("X509CredentialsProvider - clientBootstrap and tlsContext must be non null");
        }

        long nativeHandle = x509CredentialsProviderNew(this, clientBootstrap.getNativeHandle(), tlsContext.getNativeHandle(), thingName.getBytes(UTF8), roleAlias.getBytes(UTF8), endpoint.getBytes(UTF8));

        acquireNativeHandle(nativeHandle);
        addReferenceTo(clientBootstrap);
        addReferenceTo(tlsContext);
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long x509CredentialsProviderNew(X509CredentialsProvider thisObj, long bootstrapHandle, long tlsContextHandle, byte[] thingName, byte[] roleAlias, byte[] endpoint);
}
