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
package software.amazon.awssdk.crt;

import software.amazon.awssdk.crt.CRT;
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
        acquire(socket_options_new());
    }

    @Override
    public void close() {
        if (native_ptr() != 0) {
            socket_options_clean_up(release());
        }
    }

    void setDomain(SocketDomain domain) {
        socket_options_set_domain(native_ptr(), domain.getValue());
    }

    void setType(SocketType type) {
        socket_options_set_type(native_ptr(), type.getValue());
    }

    void setConnectTimeoutMs(int timeoutMs) {
        socket_options_set_connect_timeout_ms(native_ptr(), timeoutMs);
    }

    void setKeepAliveIntervalSeconds(short intervalSeconds) {
        socket_options_set_keep_alive_interval_sec(native_ptr(), intervalSeconds);
    }

    void setKeepAliveTimeoutSeconds(short timeoutSeconds) {
        socket_options_set_keep_alive_timeout_sec(native_ptr(), timeoutSeconds);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long socket_options_new() throws CrtRuntimeException;

    private static native void socket_options_clean_up(long elg);
    
    private static native void socket_options_set_domain(long tls, int domain);

    private static native void socket_options_set_type(long tls, int type);

    private static native void socket_options_set_connect_timeout_ms(long tls, int connect_timeout_ms);

    private static native void socket_options_set_keep_alive_interval_sec(long tls, short keep_alive_interval);

    private static native void socket_options_set_keep_alive_timeout_sec(long tls, short keep_alive_timeout);
};
