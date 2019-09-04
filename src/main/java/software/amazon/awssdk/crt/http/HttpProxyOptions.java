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

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.TlsConnectionOptions;

/**
 * This class provides access to Http Proxy configuration options
 */
public class HttpProxyOptions extends CrtResource {

    private String host;
    private String authUsername;
    private String authPassword;
    private TlsConnectionOptions tlsConnectionOptions;

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

        int getValue() {
            return authType;
        }
    }

    /**
     * Creates a new set of proxy options
     * @throws CrtRuntimeException If the system is unable to allocate space for a http proxy options instance
     */
    public HttpProxyOptions() throws CrtRuntimeException {
        acquire(httpProxyOptionsNew());
    }

    /**
     * Frees the native resources for this set of proxy options
     */
    @Override
    public void close() {
        if (!isNull()) {
            httpProxyOptionsDestroy(release());
        }
        super.close();
    }

    /**
     * Sets the proxy host to connect through
     * @param host proxy to connect through
     */
    public void setHost(String host) {
        this.host = host;
        httpProxyOptionsSetHost(native_ptr(), this.host);
    }

    /**
     * Sets the proxy port to connect through
     * @param port proxy port to connect through
     */
    public void setPort(int port) {
        httpProxyOptionsSetPort(native_ptr(), port);
    }

    /**
     * Sets the proxy authorization type
     * @param authorizationType what kind of authentication, if any, to use
     */
    public void setAuthorizationType(HttpProxyAuthorizationType authorizationType) {
        httpProxyOptionsSetAuthorizationType(native_ptr(), authorizationType.getValue());
    }

    /**
     * Sets the username to use for authorization; only applicable to basic authentication
     * @param host username to use with basic authentication
     */
    public void setAuthorizationUsername(String username) {
        this.authUsername = username;
        httpProxyOptionsSetAuthorizationUsername(native_ptr(), this.authUsername);
    }

    /**
     * Sets the password to use for authorization; only applicable to basic authentication
     * @param host password to use with basic authentication
     */
    public void setAuthorizationPassword(String password) {
        this.authPassword = password;
        httpProxyOptionsSetAuthorizationPassword(native_ptr(), this.authPassword);
    }

    /**
     * Sets the tls connection options for the proxy connection
     * @param tlsConnectionOptions tls connection options for the proxy connection
     */
    public void setTlsConnectionOptions(TlsConnectionOptions tlsConnectionOptions) {
        this.tlsConnectionOptions = tlsConnectionOptions;
        httpProxyOptionsSetTlsConnectionOptions(native_ptr(), this.tlsConnectionOptions.native_ptr());
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long httpProxyOptionsNew() throws CrtRuntimeException;

    private static native void httpProxyOptionsDestroy(long native_proxy_options);

    private static native void httpProxyOptionsSetAuthorizationType(long native_proxy_options, int authorization_type);

    private static native void httpProxyOptionsSetHost(long native_proxy_options, String host);

    private static native void httpProxyOptionsSetPort(long native_proxy_options, int port);

    private static native void httpProxyOptionsSetAuthorizationUsername(long native_proxy_options, String username);

    private static native void httpProxyOptionsSetAuthorizationPassword(long native_proxy_options, String password);

    private static native void httpProxyOptionsSetTlsConnectionOptions(long native_proxy_options, long native_tls_options);
}
