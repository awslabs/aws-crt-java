/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

import software.amazon.awssdk.crt.CrtResource;

/**
 * An AWS MQTT service streaming operation.  A streaming operation listens to messages on
 * a particular topic, deserializes them using a service model, and emits the modeled data by invoking a callback.
 */
public class StreamingOperationBase extends CrtResource {

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
        streamingOperationOpen(getNativeHandle());
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
