/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

public class HostResolver extends CrtResource {
    private final static int DEFAULT_MAX_ENTRIES = 8;

    public HostResolver(EventLoopGroup elg) throws CrtRuntimeException {
        this(elg, DEFAULT_MAX_ENTRIES);
    }

    public HostResolver(EventLoopGroup elg, int maxEntries) throws CrtRuntimeException {
        acquireNativeHandle(hostResolverNew(elg.getNativeHandle(), maxEntries));
        addReferenceTo(elg);
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /**
     * Cleans up the resolver's associated native handle
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            hostResolverRelease(getNativeHandle());
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long hostResolverNew(long el_group, int max_entries) throws CrtRuntimeException;
    private static native void hostResolverRelease(long host_resolver);
}
