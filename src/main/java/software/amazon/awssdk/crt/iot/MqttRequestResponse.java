/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

/**
 * Encapsulates a response to an AWS IoT Core MQTT-based service request
 */
public class MqttRequestResponse {

    private final String topic;
    private final byte[] payload;

    private MqttRequestResponse(String topic, byte []payload) {
        this.topic = topic;
        this.payload = payload;
    }

    /**
     * Gets the MQTT topic that the response was received on.
     *
     * Different topics map to different types within the
     * service model, so we need this value in order to know what to deserialize the payload into.
     *
     * @return the MQTT topic that the response was received on
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Gets the payload of the response that correlates to a submitted request.
     *
     * @return Payload of the response that correlates to a submitted request.
     */
    public byte[] getPayload() {
        return payload;
    }
}
