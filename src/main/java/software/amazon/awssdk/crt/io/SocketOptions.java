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
     * Sets the socket domain
     */
    public SocketDomain domain = SocketDomain.IPv6;
    /**
     * Sets the socket type
     */
    public SocketType type = SocketType.STREAM;
    /**
     * Sets the number of milliseconds before a connection will be considered timed out
     */
    public int connectTimeoutMs = 3000;
    /**
     * Sets the number of seconds between TCP keepalive packets being sent to the peer
     * 0 disables keepalive
     */
    public int keepAliveIntervalSecs = 0;
    /**
     * Sets the number of seconds to wait for a keepalive response before considering the connection timed out
     * 0 disables keepalive
     */
    public int keepAliveTimeoutSecs = 0;

    /**
     * Creates a new set of socket options
     */
    public SocketOptions() {
        
    }

    @Override
    public long getNativeHandle() {
        if (super.getNativeHandle() == 0) {
            acquireNativeHandle(socketOptionsNew(
                domain.getValue(),
                type.getValue(),
                connectTimeoutMs,
                keepAliveIntervalSecs,
                keepAliveTimeoutSecs
            ));   
        }
        return super.getNativeHandle();
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /**
     * Frees the native resources for this set of socket options
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            socketOptionsDestroy(getNativeHandle());
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long socketOptionsNew(int domain, int type, int connectTimeoutMs, int keepAliveIntervalSecs, int keepAliveTimeoutSecs);

    private static native void socketOptionsDestroy(long elg);
};
