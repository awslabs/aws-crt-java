/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

public class IncomingPublishEvent {

    private final byte[] payload;

    private final String topic;

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
}
