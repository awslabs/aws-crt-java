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

public class MqttMessage {
    private String topic;
    private final ByteBuffer payload;

    public MqttMessage(String _topic, ByteBuffer _payload) {
        topic = _topic;
        payload = _payload;
    }

    public String getTopic() {
        return topic;
    }

    public ByteBuffer getPayload() {
        return payload;
    }

    public ByteBuffer getPayloadDirect() {
        /* If the buffer is already direct, we can avoid an additional copy */
        if (payload.isDirect()) {
            return payload;
        }

        ByteBuffer payloadDirect = ByteBuffer.allocateDirect(payload.capacity());
        payloadDirect.put(payload);
        return payloadDirect;
    }
}
