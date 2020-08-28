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

    /*
     * Static interface for access to a default, lazily-created host resolver for users who don't
     * want to deal with the associated resource management.  Client bootstraps will use this host resolver
     * if they are passed a null value.
     */

    /**
     * Sets the max number of cached host entries for the static default resolver, if it's ever created/used. Has no
     * effect if the static default host resolver has already been created.
     *
     * @param maxEntries maximum number of host entries cached
     */
    public static void setStaticDefaultMaxEntries(int maxEntries) {
        staticDefaultMaxEntries = maxEntries;
    }

    /**
     * Closes the static default host resolver, if it exists.  Primarily intended for tests that use the static
     * default resolver, before they call waitForNoResources().
     */
    public static void closeStaticDefault() {
        synchronized (HostResolver.class) {
            if (staticDefaultResolver != null) {
                staticDefaultResolver.close();
            }
            staticDefaultResolver = null;
        }
    }

    /**
     * Gets the static default host resolver, creating it if necessary
     * @return the static default host resolver
     */
    static HostResolver getOrCreateStaticDefault() {
        HostResolver resolver = null;
        synchronized (HostResolver.class) {
            if (staticDefaultResolver == null) {
                staticDefaultResolver = new HostResolver(EventLoopGroup.getOrCreateStaticDefault(), staticDefaultMaxEntries);
            }

            resolver = staticDefaultResolver;
        }

        return resolver;
    }

    private static int staticDefaultMaxEntries = DEFAULT_MAX_ENTRIES;
    private static HostResolver staticDefaultResolver;

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long hostResolverNew(long el_group, int max_entries) throws CrtRuntimeException;
    private static native void hostResolverRelease(long host_resolver);
}
