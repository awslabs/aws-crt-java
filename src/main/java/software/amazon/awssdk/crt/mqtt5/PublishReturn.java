/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket;

/**
 * The data returned when a publish is made to a topic the MQTT5 client is subscribed to.
 * The data contained within can be gotten using the <code>get</code> functions.
 * For example, <code>getPublishPacket</code> will return the PublishPacket received from the server.
 */
public class PublishReturn {
    private PublishPacket publishPacket;

    /**
     * Single-element long array holding the native manual PUBACK control context pointer.
     * Element [0] is the pointer value, valid only during the
     * {@link Mqtt5ClientOptions.PublishEvents#onMessageReceived} callback.
     * QoS 0 results in this being set to 0.
     * Native code sets [0] to 0 after the callback returns (via SetLongArrayRegion,
     * requiring no extra JNI method ID).
     */
    private final long[] nativeContextPtrHolder;

    /**
     * Returns the PublishPacket returned from the server or Null if none was returned.
     * @return The PublishPacket returned from the server.
     */
    public PublishPacket getPublishPacket() {
        return publishPacket;
    }

    /**
     * Acquires manual control over the PUBACK for this QoS 1 PUBLISH message, preventing the
     * client from automatically sending a PUBACK. The returned handle can be passed to
     * {@link Mqtt5Client#invokePuback(Mqtt5PubackControlHandle)} at a later time to send the
     * PUBACK to the broker.
     *
     * <p><b>Important:</b> This method must be called within the
     * {@link Mqtt5ClientOptions.PublishEvents#onMessageReceived} callback. Calling it after the
     * callback returns will throw an {@link IllegalStateException}.</p>
     *
     * <p>This method may only be called once per received PUBLISH. Subsequent calls will throw
     * an {@link IllegalStateException}.</p>
     *
     * <p>If this method is not called, the client will automatically send a PUBACK for QoS 1
     * messages when the callback returns.</p>
     *
     * @return A {@link Mqtt5PubackControlHandle} that can be used to manually send the PUBACK.
     * @throws IllegalStateException if called outside the onMessageReceived callback or called more than once.
     */
    public synchronized Mqtt5PubackControlHandle acquirePubackControl() {
        if (nativeContextPtrHolder == null || nativeContextPtrHolder[0] == 0) {
            throw new IllegalStateException(
                "acquirePubackControl() must be called within the onMessageReceived callback and may only be called once.");
        }
        long controlId = mqtt5AcquirePubackControl(nativeContextPtrHolder[0]);
        /* We set the array element to 0 so it can't be double-called */
        nativeContextPtrHolder[0] = 0;
        return new Mqtt5PubackControlHandle(controlId);
    }

    /**
     * This is only called in JNI to make a new PublishReturn with a PUBLISH packet.
     * The nativeContextPtrHolder is a single-element long array; native code sets [0] to 0
     * after the onMessageReceived callback returns to prevent use-after-free.
     *
     * @param newPublishPacket The PublishPacket data received from the server.
     * @param nativeContextPtrHolder Single-element long[] holding the native PUBACK control context pointer.
     */
    private PublishReturn(PublishPacket newPublishPacket, long[] nativeContextPtrHolder) {
        this.publishPacket = newPublishPacket;
        this.nativeContextPtrHolder = nativeContextPtrHolder;
    }

    /**
     * Calls the native aws_mqtt5_client_acquire_puback function.
     * @param nativeContextPtr Pointer to the native manual PUBACK control context.
     * @return The native puback control ID.
     */
    private static native long mqtt5AcquirePubackControl(long nativeContextPtr);
}