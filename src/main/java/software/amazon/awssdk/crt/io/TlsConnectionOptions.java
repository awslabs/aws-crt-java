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
 * This class wraps the aws_tls_connection_options from aws-c-io to provide
 * access to TLS configuration contexts in the AWS Common Runtime.
 */
public final class TlsConnectionOptions extends CrtResource {

    private final TlsContext context;

    /**
     * Creates a new set of connection options that can be used to create connections
     * @throws CrtRuntimeException If the system is not able to allocate space for a native tls connection options structure
     */
    public TlsConnectionOptions(TlsContext tlsContext) throws CrtRuntimeException {
        this.context = tlsContext;
        acquire(tlsConnectionOptionsNew(tlsContext.native_ptr()));
    }

    /**
     * Frees the native resources associated with this instance
     */
    @Override
    public void close() {
        if (!isNull()) {
            tlsConnectionOptionsDestroy(release());
        }
        super.close();
    }

    /**
     * Sets server name to use for the SNI extension (supported everywhere), as well as x.509 validation.
     * @param serverName server name to use for TLS endpoint validation
     */
    void setServerName(String serverName) throws CrtRuntimeException {
        tlsConnectionOptionsSetServerName(native_ptr(), serverName);
    }

    /**
     * Sets the password to use for authorization; only applicable to basic authentication
     * @param host password to use with basic authentication
     */
    void setAlpnList(String alpnList) throws CrtRuntimeException {
        tlsConnectionOptionsSetAlpnList(native_ptr(), alpnList);
    }


    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long tlsConnectionOptionsNew(long native_context) throws CrtRuntimeException;

    private static native void tlsConnectionOptionsDestroy(long elg);

    private static native void tlsConnectionOptionsSetServerName(long native_options, String server_name) throws CrtRuntimeException;

    private static native void tlsConnectionOptionsSetAlpnList(long native_options, String alpn_list) throws CrtRuntimeException;
};
