/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.TlsContext;

/**
 * This class provides access to Http proxy configuration options
 */
public class HttpProxyOptions {

    private String host;
    private int port;
    private String authUsername;
    private String authPassword;
    private TlsContext tlsContext;
    private HttpProxyAuthorizationType authorizationType;
    private HttpProxyConnectionType connectionType;

    /**
     * what kind of authentication, if any, to use when connecting to a proxy server
     */
    public enum HttpProxyAuthorizationType {
        /**
         * No authentication
         */
        None(0),

        /**
         * Basic (username and password base64 encoded) authentication
         */
        Basic(1);

        private int authType;

        HttpProxyAuthorizationType(int val) {
            authType = val;
        }

        public int getValue() {
            return authType;
        }
    }

    /**
     * What kind of proxy connection to establish
     */
    public enum HttpProxyConnectionType {
        /**
         * The legacy default connection type:
         *   (1) If Tls is being used to connect to the endpoint, use tunneling
         *   (2) otherwise use forwarding
         */
        Legacy(0),

        /**
         * Establish a forwarding-based connection through the proxy.  It is invalid to use tls with
         * a forwarding connection
         */
        Forwarding(1),

        /**
         * Establish a tunneling-based connection through the proxy.
         */
        Tunneling(2);

        private int connectionType;

        HttpProxyConnectionType(int val) {
            connectionType = val;
        }

        public int getValue() {
            return connectionType;
        }

    }

    /**
     * Creates a new set of proxy options
     * @throws CrtRuntimeException If the system is unable to allocate space for a http proxy options instance
     */
    public HttpProxyOptions() {
        this.authorizationType = HttpProxyAuthorizationType.None;
        this.connectionType = HttpProxyConnectionType.Legacy;
    }

    /**
     * Sets the proxy connection type
     * @param connectionType what kind of connection to establish
     */
    public void setConnectionType(HttpProxyConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * @return the proxy connection type
     */
    public HttpProxyConnectionType getConnectionType() { return connectionType; }

    /**
     * Sets the proxy host to connect through
     * @param host proxy to connect through
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the proxy host to connect through
     */
    public String getHost() { return host; }

    /**
     * Sets the proxy port to connect through
     * @param port proxy port to connect through
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the proxy port to connect through
     */
    public int getPort() { return port; }

    /**
     * Sets the proxy authorization type
     * @param authorizationType what kind of authentication, if any, to use
     */
    public void setAuthorizationType(HttpProxyAuthorizationType authorizationType) {
        this.authorizationType = authorizationType;
    }

    /**
     * @return the proxy authorization type
     */
    public HttpProxyAuthorizationType getAuthorizationType() { return authorizationType; }

    /**
     * Sets the username to use for authorization; only applicable to basic authentication
     * @param username username to use with basic authentication
     */
    public void setAuthorizationUsername(String username) {
        this.authUsername = username;
    }

    /**
     * @return the username to use for authorization
     */
    public String getAuthorizationUsername() { return authUsername; }

    /**
     * Sets the password to use for authorization; only applicable to basic authentication
     * @param password password to use with basic authentication
     */
    public void setAuthorizationPassword(String password) {
        this.authPassword = password;
    }

    /**
     * @return the password to use for authorization
     */
    public String getAuthorizationPassword() { return authPassword; }

    /**
     * Sets the tls context for the proxy connection
     * @param tlsContext tls context for the proxy connection
     */
    public void setTlsContext(TlsContext tlsContext) {
        this.tlsContext = tlsContext;
    }

    /**
     * @return the tls context for the proxy connection
     */
    public TlsContext getTlsContext() { return tlsContext; }

}
