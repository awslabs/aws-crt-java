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
        acquireNativeHandle(eventLoopGroupNew(numThreads));
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
            eventLoopGroupDestroy(this, getNativeHandle());
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

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long eventLoopGroupNew(int numThreads) throws CrtRuntimeException;
    private static native void eventLoopGroupDestroy(EventLoopGroup thisObj, long elg);
};
