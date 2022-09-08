/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.http.HttpProxyOptions.HttpProxyConnectionType;

/**
 * This class provides access to Http proxy environment variable configuration
 * options
 */
public class HttpProxyEnvironmentVariableOptions {

    private TlsContext tlsContext;
    private HttpProxyEnvironmentVariableSetting environmentVariableSetting;
    private HttpProxyConnectionType connectionType;

    /*
     * Configuration for using proxy from environment variable.
     */
    public enum HttpProxyEnvironmentVariableSetting {

        /**
         * Disable reading from environment variable for proxy.
         */
        AWS_HPEV_DISABLE(0),

        /**
         * Default.
         * Enable get proxy URL from environment variable, when the manual proxy options
         * of connection manager is not set.
         * env HTTPS_PROXY/https_proxy will be checked when the main connection use tls.
         * env HTTP_PROXY/http_proxy will be checked when the main connection NOT use
         * tls.
         * The lower case version has precedence.
         */
        AWS_HPEV_ENABLE(1);

        private int environmentVariableType;

        HttpProxyEnvironmentVariableSetting(int val) {
            environmentVariableType = val;
        }

        public int getValue() {
            return environmentVariableType;
        }
    }

    /**
     * Creates a new set of environment variable proxy options
     * By Default environmentVariableSetting is set to Enable.
     *
     * @throws CrtRuntimeException If the system is unable to allocate space for a
     *                             http environment variable proxy options instance
     */
    public HttpProxyEnvironmentVariableOptions() {
        this.environmentVariableSetting = HttpProxyEnvironmentVariableSetting.AWS_HPEV_ENABLE;
        this.connectionType = HttpProxyConnectionType.Legacy;
    }

    /**
     * Sets the proxy connection type
     *
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
     * @return the http proxy environment variable setting
     */
    public HttpProxyEnvironmentVariableSetting getEnvironmentVariableSetting() {
        return environmentVariableSetting;
    }

    /**
     * Sets the http proxy environment variable setting
     *
     * @param environmentVariableSetting
     */
    public void setEnvironmentVariableSetting(HttpProxyEnvironmentVariableSetting environmentVariableSetting) {
        this.environmentVariableSetting = environmentVariableSetting;
    }

    /**
     * Sets the tls context for the proxy connection
     *
     * @param tlsContext tls context for the proxy connection
     */
    public void setTlsContext(TlsContext tlsContext) {
        this.tlsContext = tlsContext;
    }

    /**
     * @return the tls context for the proxy connection
     */
    public TlsContext getTlsContext() {
        return tlsContext;
    }

}
