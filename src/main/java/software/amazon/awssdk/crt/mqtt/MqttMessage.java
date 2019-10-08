/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.awssdk.crt.mqtt;

import java.nio.ByteBuffer;
import software.amazon.awssdk.crt.utils.ByteBufferUtils;

/**
 * Represents a single message to be published or that was published to a connection
 */
public final class MqttMessage {
    private String topic;
    private final ByteBuffer payload;

    /**
     * Constructs a new payload
     * @param topic The topic this message is to be published on or was published to
     * @param payload The message payload. Note that for minimal copies/maximum efficiency, this should be a direct
     *                buffer allocated via {@link java.nio.ByteBuffer#allocateDirect(int)}
     */
    public MqttMessage(String topic, ByteBuffer payload) {
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
    public ByteBuffer getPayload() {
        return payload;
    }

    // Used mostly internally to retrieve the payload in order to pass it to native code
    ByteBuffer getPayloadDirect() {
        return ByteBufferUtils.toDirectBuffer(payload);
    }
}
