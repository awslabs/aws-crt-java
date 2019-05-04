/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

/**
 * This class wraps the aws_socket_options from aws-c-io to provide
 * access to TCP/UDP socket configuration in the AWS Common Runtime.
 */
public final class SocketOptions extends CrtResource {

    /**
     * Socket communications domain
     */
    public enum SocketDomain {
        /**
         * Corresponds to PF_INET in Berkeley sockets
         */
        IPv4(0),
        /**
         * Corresponds to PF_INET6 in Berkeley sockets
         */
        IPv6(1),
        /**
         * Corresponds to PF_LOCAL in Berkeley sockets, usually UNIX domain sockets or named pipes
         */
        LOCAL(2);

        private int domain;

        SocketDomain(int val) {
            domain = val;
        }

        int getValue() {
            return domain;
        }
    }

    /**
     * Socket type
     */
    public enum SocketType {
        /**
         * Corresponds to SOCK_STREAM in Berkeley sockets (TCP)
         */
        STREAM(0),
        /**
         * Corresponds to SOCK_DGRAM in Berkeley sockets (UDP)
         */
        DGRAM(1);

        private int type;

        SocketType(int val) {
            type = val;
        }

        int getValue() {
            return type;
        }
    }

    /**
     * Creates a new set of socket options
     * @throws CrtRuntimeException If the system is unable to allocate space for a native socket options instance
     */
    public SocketOptions() throws CrtRuntimeException {
        acquire(socketOptionsNew());
    }

    /**
     * Frees the native resources for this set of socket options
     */
    @Override
    public void close() {
        if (!isNull()) {
            socketOptionsDestroy(release());
        }
        super.close();
    }

    /**
     * Sets the socket domain
     * @param domain
     */
    void setDomain(SocketDomain domain) {
        socketOptionsSetDomain(native_ptr(), domain.getValue());
    }

    /**
     * Sets the socket type
     * @param type
     */
    void setType(SocketType type) {
        socketOptionsSetType(native_ptr(), type.getValue());
    }

    /**
     * Sets the number of milliseconds before a connection will be considered timed out
     * @param timeoutMs The amount of time, in milliseconds, to wait for a connection to complete
     */
    void setConnectTimeoutMs(int timeoutMs) {
        socketOptionsSetConnectTimeoutMs(native_ptr(), timeoutMs);
    }

    /**
     * Sets the number of seconds between TCP keepalive packets being sent to the peer
     * @param intervalSeconds The amount of time, in seconds, between keepalive packet sends
     */
    void setKeepAliveIntervalSeconds(short intervalSeconds) {
        socketOptionsSetKeepAliveIntervalSec(native_ptr(), intervalSeconds);
    }

    /**
     * Sets the number of seconds to wait for a keepalive response before considering the connection timed out
     * @param timeoutSeconds The amount of time, in seconds, to wait for a keepalive to be acknowledged by the peer
     *                       before timing out the socket connection
     */
    void setKeepAliveTimeoutSeconds(short timeoutSeconds) {
        socketOptionsSetKeepAliveTimeoutSec(native_ptr(), timeoutSeconds);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long socketOptionsNew() throws CrtRuntimeException;

    private static native void socketOptionsDestroy(long elg);
    
    private static native void socketOptionsSetDomain(long tls, int domain);

    private static native void socketOptionsSetType(long tls, int type);

    private static native void socketOptionsSetConnectTimeoutMs(long tls, int connect_timeout_ms);

    private static native void socketOptionsSetKeepAliveIntervalSec(long tls, short keep_alive_interval);

    private static native void socketOptionsSetKeepAliveTimeoutSec(long tls, short keep_alive_timeout);
};
