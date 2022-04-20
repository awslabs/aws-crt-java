/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CleanableCrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

/**
 * This class wraps the aws_tls_context from aws-c-io to provide
 * access to TLS configuration contexts in the AWS Common Runtime.
 */
public class TlsContext extends CleanableCrtResource {

    /**
     * Creates a new Client TlsContext. There are significant native resources consumed to create a TlsContext, so most
     * applications will only need to create one and re-use it for all connections.
     * @param options A set of options for this context
     * @throws CrtRuntimeException If the provided options are malformed or the system is unable
     * to allocate space for a native tls context
     */
    public TlsContext(TlsContextOptions options) throws CrtRuntimeException {
        acquireNativeHandle(tlsContextNew(options.getNativeHandle()), TlsContext::tlsContextDestroy);
    }

    /**
     * Creates a new Client TlsContext. There are significant native resources consumed to create a TlsContext, so most
     * applications will only need to create one and re-use it for all connections.
     */
    public TlsContext() throws CrtRuntimeException  {
        try (TlsContextOptions options = TlsContextOptions.createDefaultClient()) {
            acquireNativeHandle(tlsContextNew(options.getNativeHandle()), TlsContext::tlsContextDestroy);
        }
    }

    protected static native long tlsContextNew(long options) throws CrtRuntimeException;

    private static native void tlsContextDestroy(long elg);
};
