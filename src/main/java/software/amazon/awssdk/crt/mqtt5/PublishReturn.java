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
     * The already-acquired publish acknowledgement control ID, eagerly acquired by native code
     * before the {@link Mqtt5ClientOptions.PublishEvents#onMessageReceived} callback is invoked.
     * For QoS 0 messages this is 0 (no PUBACK needed).
     * After {@code acquirePublishAcknowledgementControl()} is called, this is set to 0 to prevent
     * double-use.
     */
    private long controlId;

    /**
     * Set to true when {@code acquirePublishAcknowledgementControl()} is called, indicating that
     * the user has taken manual control of the publish acknowledgement. Native code reads this
     * via {@code wasControlAcquired()} after the callback returns to decide whether to
     * auto-invoke the PUBACK.
     */
    private boolean controlAcquired;

    /**
     * Returns the PublishPacket returned from the server or Null if none was returned.
     * @return The PublishPacket returned from the server.
     */
    public PublishPacket getPublishPacket() {
        return publishPacket;
    }

    /**
     * Acquires manual control over the publish acknowledgement (PUBACK) for this PUBLISH message,
     * preventing the client from automatically sending an acknowledgement. The returned handle can be
     * passed to {@link Mqtt5Client#invokePublishAcknowledgement(Mqtt5PublishAcknowledgementControlHandle)}
     * at a later time to send the PUBACK to the broker.
     *
     * <p>The PUBACK control is eagerly acquired by the native layer as soon as the publish is received,
     * before this callback is invoked. Calling this method retrieves the pre-acquired control handle.</p>
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
     * @return A {@link Mqtt5PublishAcknowledgementControlHandle} that can be used to manually send the acknowledgement.
     * @throws IllegalStateException if called outside the onMessageReceived callback, called more than once,
     *                               or called on a QoS 0 message.
     */
    public synchronized Mqtt5PublishAcknowledgementControlHandle acquirePublishAcknowledgementControl() {
        if (controlId == 0) {
            throw new IllegalStateException(
                "acquirePublishAcknowledgementControl() must be called within the onMessageReceived callback and may only be called once.");
        }
        long acquiredControlId = controlId;
        /* Zero out so it can't be double-called */
        controlId = 0;
        controlAcquired = true;
        return new Mqtt5PublishAcknowledgementControlHandle(acquiredControlId);
    }

    /**
     * Returns whether the user called {@link #acquirePublishAcknowledgementControl()} during the
     * {@link Mqtt5ClientOptions.PublishEvents#onMessageReceived} callback.
     *
     * <p>This is called by native/JNI code after the callback returns to determine whether to
     * automatically invoke the publish acknowledgement (PUBACK). If this returns {@code false},
     * native code will auto-invoke the PUBACK; if {@code true}, the user is responsible for
     * calling {@link Mqtt5Client#invokePublishAcknowledgement(Mqtt5PublishAcknowledgementControlHandle)}.</p>
     *
     * @return {@code true} if the user acquired manual control of the PUBACK, {@code false} otherwise.
     */
    boolean wasControlAcquired() {
        return controlAcquired;
    }

    /**
     * This is only called in JNI to make a new PublishReturn with a PUBLISH packet.
     * The controlId is the already-acquired publish acknowledgement control ID (eagerly acquired
     * by native code before the callback fires). It is 0 for QoS 0 messages.
     *
     * @param newPublishPacket The PublishPacket data received from the server.
     * @param controlId The pre-acquired publish acknowledgement control ID (0 for QoS 0 messages).
     */
    private PublishReturn(PublishPacket newPublishPacket, long controlId) {
        this.publishPacket = newPublishPacket;
        this.controlId = controlId;
        this.controlAcquired = false;
    }
}
