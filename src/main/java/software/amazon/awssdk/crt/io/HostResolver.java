/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

public class HostResolver extends CrtResource {
    private final static int DEFAULT_MAX_ENTRIES = 8;
    private final EventLoopGroup elg;
    private final int maxEntries;

    public HostResolver(EventLoopGroup elg) throws CrtRuntimeException {
        this(elg, DEFAULT_MAX_ENTRIES);
    }

    public HostResolver(EventLoopGroup elg, int maxEntries) throws CrtRuntimeException {
        this.elg = elg;
        this.maxEntries = maxEntries;
        acquire(hostResolverNew(elg.native_ptr(), maxEntries));
    }

    @Override
    public void close() {
        if (!isNull()) {
            hostResolverRelease(release());
        }
        super.close();
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long hostResolverNew(long el_group, int max_entries) throws CrtRuntimeException;
    private static native void hostResolverRelease(long host_resolver);
}
