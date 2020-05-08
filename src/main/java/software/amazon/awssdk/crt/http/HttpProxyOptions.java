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
     * Creates a new set of proxy options
     * @throws CrtRuntimeException If the system is unable to allocate space for a http proxy options instance
     */
    public HttpProxyOptions() {
        this.authorizationType = HttpProxyAuthorizationType.None;
    }

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
