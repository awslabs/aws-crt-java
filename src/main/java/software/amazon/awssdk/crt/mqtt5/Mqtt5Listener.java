/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;


 /**
 * This class wraps the aws-c-mqtt MQTT5 client to provide the basic MQTT5 pub/sub functionalities
 * via the AWS Common Runtime
 *
 * One Mqtt5Listener class creates one connection.
 *
 * MQTT5 support is currently in <b>developer preview</b>.  We encourage feedback at all times, but feedback during the
 * preview window is especially valuable in shaping the final product.  During the preview period we may make
 * backwards-incompatible changes to the public API, but in general, this is something we will try our best to avoid.
 */
public class Mqtt5Listener extends CrtResource {

    /**
     * Creates a Mqtt5Listener instance using the provided Mqtt5ListenerOptions. Once the Mqtt5Listener is created,
     * changing the settings will not cause a change in already created Mqtt5Listener's.
     *
     * @param options The Mqtt5ListenerOptions class to use to configure the new Mqtt5Listener.
     * @param client The Mqtt5Client class the mqtt5 listener listen to
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT5 client structure
     */
    public Mqtt5Listener(Mqtt5ListenerOptions options, Mqtt5Client client) throws CrtRuntimeException {

        acquireNativeHandle(mqtt5ListenerNew(
            options,
            client,
            this
        ));
    }

    /**
     * Cleans up the native resources associated with this client. The client is unusable after this call
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            mqtt5ListenerDestroy(getNativeHandle());
        }
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return false; }


    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long mqtt5ListenerNew(
        Mqtt5ListenerOptions options,
        Mqtt5Client client,
        Mqtt5Listener listener
    ) throws CrtRuntimeException;
    private static native void mqtt5ListenerDestroy(long listener);

}
