/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt;

/**
 * Represents a message to publish, or a message that was received.
 */
public final class MqttMessage {
    private String topic;
    private final byte[] payload;
    private QualityOfService qos;
    private boolean retain;
    private boolean dup;

    /**
     * Constructs a new message.
     *
     * @param topic   Message topic.
     * @param payload Message payload.
     * @param qos     {@link QualityOfService}. When sending, the
     *                {@link QualityOfService} to use for delivery. When receiving,
     *                the {@link QualityOfService} used for delivery.
     * @param retain  Retain flag. When sending, whether the message should be
     *                retained by the broker and delivered to future subscribers.
     *                When receiving, whether the message was sent as a result of a
     *                new subscription being made.
     * @param dup     DUP flag. Ignored when sending. When receiving, indicates
     *                whether this might be re-delivery of an earlier attempt to
     *                send the message.
     */
    public MqttMessage(String topic, byte[] payload, QualityOfService qos, boolean retain, boolean dup) {
        this.topic = topic;
        this.payload = payload;
        this.dup = dup;
        this.qos = qos;
        this.retain = retain;
    }

    /**
     * Constructs a new message.
     *
     * @param topic   Message topic.
     * @param payload Message payload.
     * @param qos     {@link QualityOfService}. When sending, the
     *                {@link QualityOfService} to use for delivery. When receiving,
     *                the {@link QualityOfService} used for delivery.
     * @param retain  Retain flag. When sending, whether the message should be
     *                retained by the broker and delivered to future subscribers.
     *                When receiving, whether the message was sent as a result of a
     *                new subscription being made.
     */
    public MqttMessage(String topic, byte[] payload, QualityOfService qos, boolean retain) {
        this(topic, payload, qos, retain, false);
    }

    /**
     * Constructs a new message.
     *
     * @param topic   Message topic.
     * @param payload Message payload.
     * @param qos     {@link QualityOfService}. When sending, the
     *                {@link QualityOfService} to use for delivery. When receiving,
     *                the {@link QualityOfService} used for delivery.
     */
    public MqttMessage(String topic, byte[] payload, QualityOfService qos) {
        this(topic, payload, qos, false, false);
    }

    @Deprecated
    public MqttMessage(String topic, byte[] payload) {
        this(topic, payload, QualityOfService.AT_LEAST_ONCE, false, false);
    }

    /**
     * Gets the topic associated with this message
     * @return The topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Gets the message payload
     * @return Message payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Gets the {@link QualityOfService}. When sending, the {@link QualityOfService}
     * to use for delivery. When receiving, the {@link QualityOfService} used for
     * delivery.
     *
     * @return The {@link QualityOfService}
     */
    public QualityOfService getQos() {
        return qos;
    }

    /**
     * Gets the retain flag. When sending, whether the message should be retained by
     * the broker and delivered to future subscribers. When receiving, whether the
     * message was sent as a result of a new subscription being made.
     *
     * @return Retain flag
     */
    public boolean getRetain() {
        return retain;
    }

    /**
     * Gets the DUP flag. Ignored when sending. When receiving, indicates whether
     * this might be re-delivery of an earlier attempt to send the message.
     *
     * @return DUP flag
     */
    public boolean getDup() {
        return dup;
    }

}
