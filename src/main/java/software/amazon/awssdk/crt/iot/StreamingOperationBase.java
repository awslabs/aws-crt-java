/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

import software.amazon.awssdk.crt.CrtResource;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An AWS MQTT service streaming operation.  A streaming operation listens to messages on
 * a particular topic, deserializes them using a service model, and emits the modeled data by invoking a callback.
 */
public class StreamingOperationBase extends CrtResource {

    /*
     * Using a read-write lock to protect the native handle on Java -> Native calls is a new approach to handle
     * accidental misuse of CrtResource objects.  The current method (no protection) works as long as the user
     * follows the rules, but race conditions can lead to crashes or undefined behavior if the user breaks the
     * rules (uses the CrtResource after or while the final close() call is in progress).
     *
     * For this new method to be correct, it must not be possible that Java -> Native calls ever call back
     * native -> Java in the same call stack.  This is true for both the request response client and streaming
     * operations, allowing us to add this layer of safety.
     */
    private final ReentrantReadWriteLock handleLock = new ReentrantReadWriteLock();
    private final Lock handleReadLock = handleLock.readLock();
    private final Lock handleWriteLock = handleLock.writeLock();

    StreamingOperationBase(MqttRequestResponseClient rrClient, StreamingOperationOptions options) {
        acquireNativeHandle(streamingOperationNew(
            this,
            rrClient.getNativeHandle(),
            options
        ));
    }

    /**
     * Triggers the streaming operation to start listening to the configured stream of events.  Has no effect on an
     * already-open operation.  It is an error to attempt to re-open a closed streaming operation.
     */
    public void open() {
        this.handleReadLock.lock();
        try {
            streamingOperationOpen(getNativeHandle());
        } finally {
            this.handleReadLock.unlock();
        }
    }

    /**
     * Cleans up the native resources associated with this client. The client is unusable after this call
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            streamingOperationDestroy(getNativeHandle());
        }
    }

    @Override
    public void close() {
        this.handleWriteLock.lock();
        try {
            super.close();
        } finally {
            this.handleWriteLock.unlock();
        }
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /*******************************************************************************
     * native methods
     ******************************************************************************/

    private static native long streamingOperationNew(StreamingOperationBase streamingOperation, long rrClientHandle, StreamingOperationOptions options);

    private static native void streamingOperationOpen(long streamingOperationHandle);

    private static native void streamingOperationDestroy(long streamingOperationHandle);
}
