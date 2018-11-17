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
import software.amazon.awssdk.crt.EventLoopGroup;

import java.io.Closeable;

/**
 * This class wraps the aws_client_bootstrap from aws-c-io to provide
 * a client context for all protocol stacks in the AWS Common Runtime.
 */
public final class ClientBootstrap extends CrtResource implements Closeable {
    private EventLoopGroup elg;

    public ClientBootstrap(EventLoopGroup _elg) throws CrtRuntimeException {
        elg = _elg;
        acquire(client_bootstrap_new(elg.native_ptr()));
    }

    @Override
    public void close() {
        if (native_ptr() != 0) {
            client_bootstrap_clean_up(release());
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long client_bootstrap_new(long elg) throws CrtRuntimeException;
    private static native void client_bootstrap_clean_up(long bootstrap);
};
