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

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.CrtResource;

import java.io.Closeable;

/**
 * This class wraps the aws_socket_options from aws-c-io to provide
 * access to socket configuration in the AWS Common Runtime.
 */
public final class SocketOptions extends CrtResource implements Closeable {

    public enum SocketDomain {
        IPV4(0), IPv6(1), LOCAL(2);

        private int domain;

        SocketDomain(int val) {
            domain = val;
        }

        int getValue() {
            return domain;
        }
    }

    public enum SocketType {
        STREAM(0), DGRAM(1);

        private int type;

        SocketType(int val) {
            type = val;
        }

        int getValue() {
            return type;
        }
    }

    public SocketOptions() throws CrtRuntimeException {
        acquire(socketOptionsNew());
    }

    @Override
    public void close() {
        if (native_ptr() != 0) {
            socketOptionsDestroy(release());
        }
    }

    void setDomain(SocketDomain domain) {
        socketOptionsSetDomain(native_ptr(), domain.getValue());
    }

    void setType(SocketType type) {
        socketOptionsSetType(native_ptr(), type.getValue());
    }

    void setConnectTimeoutMs(int timeoutMs) {
        socketOptionsSetConnectTimeoutMs(native_ptr(), timeoutMs);
    }

    void setKeepAliveIntervalSeconds(short intervalSeconds) {
        socketOptionsSetKeepAliveIntervalSec(native_ptr(), intervalSeconds);
    }

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
