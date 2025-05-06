/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

import software.amazon.awssdk.crt.mqtt5.packets.UserProperty;

import java.util.List;

/**
 * An event that describes an incoming publish message received on a streaming operation.
 */
public class IncomingPublishEvent {

    private final byte[] payload;

    private final String topic;

    private String contentType;
    private List<UserProperty> userProperties;
    private Long messageExpiryIntervalSeconds;


    private IncomingPublishEvent(byte[] payload, String topic) {
        this.payload = payload;
        this.topic = topic;
    }

    /**
     * Gets the payload of the IncomingPublishEvent.
     *
     * @return Payload of the IncomingPublishEvent.
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Gets the topic of the IncomingPublishEvent.
     *
     * @return Topic of the IncomingPublishEvent.
     */
    public String getTopic() {
        return topic;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<UserProperty> getUserProperties() {
        return userProperties;
    }

    public Long getMessageExpiryIntervalSeconds() {
        return messageExpiryIntervalSeconds;
    }
}
