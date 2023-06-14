/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.io.TlsConnectionOptions;
import software.amazon.awssdk.crt.http.HttpProxyOptions.HttpProxyConnectionType;

/**
 * This class provides access to Http proxy environment variable configuration
 * setting
 */
public class HttpProxyEnvironmentVariableSetting {

    private TlsConnectionOptions tlsConnectionOptions;
    private HttpProxyEnvironmentVariableType environmentVariableType;
    private HttpProxyConnectionType connectionType;

    /*
     * Configuration for using proxy from environment variable.
     */
    public enum HttpProxyEnvironmentVariableType {

        /**
         * Disable reading from environment variable for proxy.
         */
        DISABLED(0),

        /**
         * Default.
         * Enable reading from environment variable for proxy configuration, when the proxy options
         * are not set.
         * env HTTPS_PROXY/https_proxy will be checked when the main connection use tls.
         * env HTTP_PROXY/http_proxy will be checked when the main connection does not use tls.
         * The lower case version has precedence.
         */
        ENABLED(1);

        private int environmentVariableType;

        HttpProxyEnvironmentVariableType(int val) {
            environmentVariableType = val;
        }

        public int getValue() {
            return environmentVariableType;
        }
    }

    /**
     * Creates a new set of environment variable proxy setting
     * By Default environmentVariableType is set to Enable.
     */
    public HttpProxyEnvironmentVariableSetting() {
        this.environmentVariableType = HttpProxyEnvironmentVariableType.ENABLED;
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
    public HttpProxyConnectionType getConnectionType() {
        return connectionType;
    }

    /**
     * @return the http proxy environment variable type
     */
    public HttpProxyEnvironmentVariableType getEnvironmentVariableType() {
        return environmentVariableType;
    }

    public void setEnvironmentVariableType(HttpProxyEnvironmentVariableType environmentVariableType) {
        this.environmentVariableType = environmentVariableType;
    }

    /**
     * Sets the tls connection options for the proxy connection
     *
     * @param tlsConnectionOptions tls connection options for the proxy connection
     */
    public void setTlsConnectionOptions(TlsConnectionOptions tlsConnectionOptions) {
        this.tlsConnectionOptions = tlsConnectionOptions;
    }

    /**
     * @return the tls connection options for the proxy connection
     */
    public TlsConnectionOptions getTlsConnectionOptions() {
        return tlsConnectionOptions;
    }

}
