/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.Log;

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
     * Sets the number of keep alive probes allowed to fail before the connection is considered lost.
     * If zero, OS defaults are used.
     * On Windows, this option is meaningless until Windows 10 1703.
     */
    public int keepAliveMaxFailedProbes = 0;

    /**
     * If true, enables periodic transmits of keepalive messages for detecting a disconnected peer.
     */
    public boolean keepAlive = false;

    /**
     * Creates a new set of socket options
     */
    public SocketOptions() {
    }

    /**
     * Enables TCP keepalive.
     *
     * @param keepAliveTimeoutSecs Sets the number of seconds to wait for a keepalive response before considering the
     * connection timed out. 0 disables keepalive.
     * @param keepAliveIntervalSecs Sets the number of seconds between TCP keepalive packets being sent to the peer.
     * 0 disables keepalive.
     */
    public void setTcpKeepAlive(int keepAliveTimeoutSecs, int keepAliveIntervalSecs)
    {
        if (keepAliveTimeoutSecs == 0 || keepAliveIntervalSecs == 0) {
            Log.log(Log.LogLevel.Warn, Log.LogSubject.IoSocket,
                    "Both keepAliveTimeoutSecs and keepAliveIntervalSecs must be non-zero in order to enable TCP keepalive");
        }
        this.keepAliveTimeoutSecs = keepAliveTimeoutSecs;
        this.keepAliveIntervalSecs = keepAliveIntervalSecs;
        this.keepAlive = true;
    }

    /**
     * Enables TCP keepalive.
     *
     * @param keepAliveTimeoutSecs Sets the number of seconds to wait for a keepalive response before considering the
     * connection timed out. 0 disables keepalive.
     * @param keepAliveIntervalSecs Sets the number of seconds between TCP keepalive packets being sent to the peer.
     * 0 disables keepalive.
     * @param keepAliveMaxFailedProbes Sets the number of keep alive probes allowed to fail before the connection is considered lost.
     * If zero, OS defaults are used.
     * On Windows, this option is meaningless until Windows 10 1703.
     */
    public void setTcpKeepAlive(int keepAliveTimeoutSecs, int keepAliveIntervalSecs, int keepAliveMaxFailedProbes)
    {
        this.keepAliveMaxFailedProbes = keepAliveMaxFailedProbes;
        setTcpKeepAlive(keepAliveTimeoutSecs, keepAliveIntervalSecs);
    }

    @Override
    public long getNativeHandle() {
        if (super.getNativeHandle() == 0) {
            acquireNativeHandle(socketOptionsNew(
                domain.getValue(),
                type.getValue(),
                connectTimeoutMs,
                keepAliveIntervalSecs,
                keepAliveTimeoutSecs,
                keepAliveMaxFailedProbes,
                keepAlive
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
    private static native long socketOptionsNew(
            int domain, int type, int connectTimeoutMs, int keepAliveIntervalSecs, int keepAliveTimeoutSecs, int keepAliveMaxFailedProbes, boolean keepAlive);

    private static native void socketOptionsDestroy(long elg);
};
