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

/**
 * This class wraps the aws_tls_context from aws-c-io to provide access to TLS
 * configuration contexts in the AWS Common Runtime.
 */
public final class ClientTlsContext extends TlsContext {

    /**
     * Creates a new Client TlsContext. There are significant native resources
     * consumed to create a TlsContext, so most applications will only need to
     * create one and re-use it for all connections.
     * 
     * @param options A set of options for this context
     * @throws CrtRuntimeException If the provided options are malformed or the
     *                             system is unable to allocate space for a native
     *                             tls context
     */
    public ClientTlsContext(TlsContextOptions options) throws CrtRuntimeException {
        super(options);
    }

    /**
     * Creates a new Client TlsContext. There are significant native resources
     * consumed to create a TlsContext, so most applications will only need to
     * create one and re-use it for all connections.
     */
    public ClientTlsContext() throws CrtRuntimeException {
        try (TlsContextOptions options = TlsContextOptions.createDefaultClient()) {
            acquireNativeHandle(tlsContextNew(options.getNativeHandle()));
        }
    }
};
