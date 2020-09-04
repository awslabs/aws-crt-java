/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.Log;

/**
 * This class wraps the aws_event_loop_group from aws-c-io to provide
 * access to an event loop for the MQTT protocol stack in the AWS Common
 * Runtime.
 */
public final class EventLoopGroup extends CrtResource {

    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    /**
     * Creates a new event loop group for the I/O subsystem to use to run blocking I/O requests
     * @param numThreads The number of threads that the event loop group may run tasks across. Usually 1.
     * @throws CrtRuntimeException If the system is unable to allocate space for a native event loop group
     */
    public EventLoopGroup(int numThreads) throws CrtRuntimeException {
        acquireNativeHandle(eventLoopGroupNew(this, numThreads));
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return false; }

    /**
     * Stops the event loop group's tasks and frees all resources associated with the the group. This should be called
     * after all other clients/connections and other resources are cleaned up, or else they will not clean up completely.
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            eventLoopGroupDestroy(getNativeHandle());
        }
    }

    /**
     * Called from Native when the asynchronous cleanup process needed for event loop groups has completed.
     */
    private void onCleanupComplete() {
        Log.log(Log.LogLevel.Trace, Log.LogSubject.IoEventLoop, "EventLoopGroup.onCleanupComplete");

        releaseReferences();

        this.shutdownComplete.complete(null);
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() { return shutdownComplete; }


    /*
     * Static interface for access to a default, lazily-created event loop group for users who don't
     * want to deal with the associated resource management.  Client bootstraps will use this event loop
     * group if they are passed a null value.
     */

    /**
     * Sets the number of threads for the static default event loop group, should it ever be created.  Has no
     * effect if the static default event loop group has already been created.
     *
     * @param numThreads number of threads for the static default event loop group
     */
    public static void setStaticDefaultNumThreads(int numThreads) {
        synchronized (EventLoopGroup.class) {
            staticDefaultNumThreads = Math.max(1, numThreads);
        }
    }

    /**
     * Closes the static default event loop group, if it exists.  Primarily intended for tests that use the static
     * default event loop group, before they call waitForNoResources().
     */
    public static void closeStaticDefault() {
        synchronized (EventLoopGroup.class) {
            if (staticDefaultEventLoopGroup != null) {
                staticDefaultEventLoopGroup.close();
            }
            staticDefaultEventLoopGroup = null;
        }
    }

    /**
     * Gets the static default event loop group, creating it if necessary
     * @return the static default event loop group
     */
    static EventLoopGroup getOrCreateStaticDefault() {
        EventLoopGroup elg = null;
        synchronized (EventLoopGroup.class) {
            if (staticDefaultEventLoopGroup == null) {
                staticDefaultEventLoopGroup = new EventLoopGroup(staticDefaultNumThreads);
            }

            elg = staticDefaultEventLoopGroup;
        }

        return elg;
    }

    private static int staticDefaultNumThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
    private static EventLoopGroup staticDefaultEventLoopGroup;

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long eventLoopGroupNew(EventLoopGroup thisObj, int numThreads) throws CrtRuntimeException;
    private static native void eventLoopGroupDestroy(long elg);
};
