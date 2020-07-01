/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt;

/**
 * Represents a single message to be published or that was published to a connection
 */
public final class MqttMessage {
    private String topic;
    private final byte[] payload;

    /**
     * Constructs a new payload
     * @param topic The topic this message is to be published on or was published to
     * @param payload The message payload. 
     */
    public MqttMessage(String topic, byte[] payload) {
        this.topic = topic;
        this.payload = payload;
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
}
