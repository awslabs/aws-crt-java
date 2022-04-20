/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CleanableCrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.Log;

/**
 * This class wraps the aws_event_loop_group from aws-c-io to provide
 * access to an event loop for the MQTT protocol stack in the AWS Common
 * Runtime.
 */
public final class EventLoopGroup extends CleanableCrtResource {

    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    /**
     * Creates a new event loop group for the I/O subsystem to use to run non-blocking I/O requests
     * @param numThreads The number of threads that the event loop group may run tasks across. Usually 1.
     * @throws CrtRuntimeException If the system is unable to allocate space for a native event loop group
     */
    public EventLoopGroup(int numThreads) throws CrtRuntimeException {
        acquireNativeHandle(eventLoopGroupNew(shutdownComplete, numThreads), EventLoopGroup::eventLoopGroupDestroy);
    }

    /**
     * Creates a new event loop group for the I/O subsystem to use to run non-blocking I/O requests. When using this
     * constructor, the threads will be pinned to a particular cpuGroup (e.g. a particular NUMA node).
     * @param cpuGroup the index of the cpu group to bind to (for example NUMA node 0 would be 0, NUMA node 1 would be 1 etc...)
     * @param numThreads The number of threads that the event loop group may run tasks across. Usually 1.
     * @throws CrtRuntimeException If the system is unable to allocate space for a native event loop group
     */
    public EventLoopGroup(int cpuGroup, int numThreads) throws CrtRuntimeException {
        acquireNativeHandle(eventLoopGroupNewPinnedToCpuGroup(shutdownComplete, cpuGroup, numThreads), EventLoopGroup::eventLoopGroupDestroy);
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
     * Closes the static EventLoopGroup, if it exists.  Primarily intended for tests that use the static
     * default EventLoopGroup, before they call waitForNoResources().
     */
    public static void closeStaticDefault() {
        synchronized (EventLoopGroup.class) {
            staticDefaultEventLoopGroup = null;
        }
    }

    /**
     * Gets the static default EventLoopGroup, creating it if necessary.
     *
     * This default will be used when a EventLoopGroup is not explicitly passed but is needed
     * to allow the process to function. An example of this would be in the MQTT connection creation workflow.
     *
     * The EventLoopGroup will automatically pick a default number of threads based on the system.
     *
     * The default EventLoopGroup will be automatically managed and released by the API handle when it's
     * resources are being freed, not requiring any manual memory management.
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
    private static native long eventLoopGroupNew(CompletableFuture<Void> shutdownCallback, int numThreads) throws CrtRuntimeException;
    private static native long eventLoopGroupNewPinnedToCpuGroup(CompletableFuture<Void> shutdownCallback, int cpuGroup, int numThreads) throws CrtRuntimeException;

    private static native void eventLoopGroupDestroy(long elg);
};
