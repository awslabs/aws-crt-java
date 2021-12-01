/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.mqtt;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * @return the native enum integer value associated with this Java enum value
     */
    public int getValue() {
        return qos;
    }

    /**
     * Creates a Java QualityOfService enum value from a native integer value
     * @param value native integer value for quality of service
     * @return a new QualityOfService value
     */
    public static QualityOfService getEnumValueFromInteger(int value) {
        QualityOfService enumValue = enumMapping.get(value);
        if (enumValue != null) {
            return enumValue;
        }
        throw new RuntimeException("Illegal QualityOfService");
    }

    private static Map<Integer, QualityOfService> buildEnumMapping() {
        return Stream.of(QualityOfService.values())
            .collect(Collectors.toMap(QualityOfService::getValue, Function.identity()));
    }

    private static Map<Integer, QualityOfService> enumMapping = buildEnumMapping();
}
