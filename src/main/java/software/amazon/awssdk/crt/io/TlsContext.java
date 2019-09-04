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
 * This class wraps the aws_tls_context from aws-c-io to provide
 * access to TLS configuration contexts in the AWS Common Runtime.
 */
public final class TlsContext extends CrtResource {

    /**
     * Creates a new Client TlsContext. There are significant native resources consumed to create a TlsContext, so most
     * applications will only need to create one and re-use it for all connections.
     * @param options A set of options for this context
     * @throws CrtRuntimeException If the provided options are malformed or the system is unable
     * to allocate space for a native tls context
     */
    public TlsContext(TlsContextOptions options) throws CrtRuntimeException {
        acquire(tlsContextNew(options.native_ptr()));
    }

    /**
     * Creates a new Client TlsContext. There are significant native resources consumed to create a TlsContext, so most
     * applications will only need to create one and re-use it for all connections.
     */
    public TlsContext() throws CrtRuntimeException  {
        acquire(tlsContextNew(own(new TlsContextOptions()).native_ptr()));
    }

    /**
     * Frees all native resources associated with the context. This object is unusable after close is called.
     */
    @Override
    public void close() {
        if (!isNull()) {
            tlsContextDestroy(release());
        }
        super.close();
    }

    public TlsConnectionOptions createConnectionOptions() throws CrtRuntimeException {
        return new TlsConnectionOptions(this);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long tlsContextNew(long options) throws CrtRuntimeException;

    private static native void tlsContextDestroy(long elg);
};
