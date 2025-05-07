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

    /**
     * Gets the content type of the IncomingPublishEvent.
     *
     * @return Content type of the IncomingPublishEvent.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the user properties of the IncomingPublishEvent.
     *
     * @return User properties of the IncomingPublishEvent.
     */
    public List<UserProperty> getUserProperties() {
        return userProperties;
    }

    /**
     * Gets the message expiry interval seconds of the IncomingPublishEvent.
     *
     * @return Message expiry interval seconds of the IncomingPublishEvent.
     */
    public Long getMessageExpiryIntervalSeconds() {
        return messageExpiryIntervalSeconds;
    }
}
