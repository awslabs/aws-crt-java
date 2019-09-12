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

/**
 * Quality of Service associated with a publish action or subscription [MQTT-4.3].
  */
public enum QualityOfService {
    /**
     * Message will be delivered at most once, or may not be delivered at all. There will be no ACK, and the message
     * will not be stored.
     */
    AT_MOST_ONCE(0),

    /**
     * Message will be delivered at least once. It may be resent multiple times if errors occur before an ACK is
     * returned to the sender. The message will be stored in case it has to be re-sent. This is the most common QualityOfService.
     */
    AT_LEAST_ONCE(1),

    /**
     * The message is always delivered exactly once. This is the safest, but slowest QualityOfService, because multiple levels
     * of handshake must happen to guarantee no duplication of messages.
     */
    EXACTLY_ONCE(2);
    /* reserved = 3 */

    private int qos;

    QualityOfService(int value) {
        qos = value;
    }

    public int getValue() {
        return qos;
    }
}
