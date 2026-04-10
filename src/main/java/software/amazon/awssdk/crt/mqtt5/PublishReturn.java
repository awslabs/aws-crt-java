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
     * For QoS 0 messages this is 0 (no publish acknowledgement required).
     * After {@code acquirePublishAcknowledgementControl()} is called, this is set to 0 to prevent
     * double-use.
     */
    private long controlId;

    /**
     * The threadID of the {@link Mqtt5ClientOptions.PublishEvents#onMessageReceived} callback.
     * This is set at the beginning of the callback and then used to verify 
     * {@code acquirePublishAcknowledgementControl()} is being called from within.
     */
    private long threadID;

    /**
     * Returns the PublishPacket returned from the server or Null if none was returned.
     * @return The PublishPacket returned from the server.
     */
    public PublishPacket getPublishPacket() {
        return publishPacket;
    }

    /**
     * Acquires manual control over the publish acknowledgement for this PUBLISH message,
     * preventing the client from automatically sending an acknowledgement. The returned handle can be
     * passed to {@link Mqtt5Client#invokePublishAcknowledgement(Mqtt5PublishAcknowledgementControlHandle)}
     * at a later time to send the publish acknowledgement to the broker.
     *
     * <p><b>Important:</b> This method must be called within the
     * {@link Mqtt5ClientOptions.PublishEvents#onMessageReceived} callback. Calling it outside the
     * callback (wrong thread) or after it has already been called will return {@code null}.</p>
     *
     * <p>This method may only be called once per received PUBLISH. Subsequent calls will return
     * {@code null}.</p>
     *
     * <p>If this method is not called, the client will automatically send a publish acknowledgment
     * for QoS 1 messages when the callback returns.</p>
     *
     * @return A {@link Mqtt5PublishAcknowledgementControlHandle} that can be used to manually send
     *         the acknowledgement, or {@code null} if called outside the callback, called more than
     *         once, or called on a QoS 0 message.
     */
    public synchronized Mqtt5PublishAcknowledgementControlHandle acquirePublishAcknowledgementControl() {
        if (controlId == 0 || threadID != Thread.currentThread().getId()) {
            return null;
        }
        long acquiredControlId = controlId;
        /* Zero out so it can't be double-called */
        controlId = 0;
        return new Mqtt5PublishAcknowledgementControlHandle(acquiredControlId);
    }

    /**
     * This is only called in JNI to make a new PublishReturn with a PUBLISH packet.
     * The controlId is eagerly acquired by native code prior to
     * {@link Mqtt5ClientOptions.PublishEvents#onMessageReceived} being called.
     * The threadID is set to the calling thread and is used when
     * {@link #acquirePublishAcknowledgementControl()} is called to guarantee the result
     * is accurate and enforces the requirement of calling it from within the callback.
     *
     * @param newPublishPacket The PublishPacket data received from the server.
     * @param controlId The pre-acquired publish acknowledgement control ID (0 for QoS 0 messages).
     */
    private PublishReturn(PublishPacket newPublishPacket, long controlId) {
        this.publishPacket = newPublishPacket;
        this.controlId = controlId;
        this.threadID = Thread.currentThread().getId();
    }
}
