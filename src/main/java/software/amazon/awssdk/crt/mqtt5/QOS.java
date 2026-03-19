/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.mqtt5;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
  * MQTT message delivery quality of service.
  *
  * Enum values match <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901234">MQTT5 spec</a> encoding values.
  */
public enum QOS {
    /**
     * The message is delivered according to the capabilities of the underlying network. No response is sent by the
     * receiver and no retry is performed by the sender. The message arrives at the receiver either once or not at all.
     */
    AT_MOST_ONCE(0),

    /**
     * A level of service that ensures that the message arrives at the receiver at least once.
     */
    AT_LEAST_ONCE(1),

    /**
     * A level of service that ensures that the message arrives at the receiver exactly once.
     */
    EXACTLY_ONCE(2);

    private int qos;

    private QOS(int value) {
        qos = value;
    }

    /**
     * @return The native enum integer value associated with this Java enum value
     */
    public int getValue() {
        return qos;
    }

    /**
     * Creates a Java QualityOfService enum value from a native integer value.
     *
     * @param value native integer value for quality of service
     * @return a new QualityOfService value
     */
    public static QOS getEnumValueFromInteger(int value) {
        QOS enumValue = enumMapping.get(value);
        if (enumValue != null) {
            return enumValue;
        }
        throw new RuntimeException("Illegal QOS");
    }

    private static Map<Integer, QOS> buildEnumMapping() {
        return Stream.of(QOS.values())
            .collect(Collectors.toMap(QOS::getValue, Function.identity()));
    }

    private static Map<Integer, QOS> enumMapping = buildEnumMapping();
}
