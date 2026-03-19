/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type of change to the state of a streaming operation subscription
 */
public enum SubscriptionStatusEventType {

    /**
     * The streaming operation is successfully subscribed to its topic (filter)
     */
    SUBSCRIPTION_ESTABLISHED(0),

    /**
     * The streaming operation has temporarily lost its subscription to its topic (filter)
     */
    SUBSCRIPTION_LOST(1),

    /**
     * The streaming operation has entered a terminal state where it has given up trying to subscribe
     * to its topic (filter).  This is always due to user error (bad topic filter or IoT Core permission policy).
     */
    SUBSCRIPTION_HALTED(2);

    private final int type;

    SubscriptionStatusEventType(int value) {
        type = value;
    }


    /**
     * @return the native enum integer value associated with this Java enum value
     */
    public int getValue() {
        return type;
    }

    /**
     * Creates a Java SubscriptionStatusEventType enum value from a native integer value
     * @param value native integer value to convert to a SubscriptionStatusEventType instance
     * @return a SubscriptionStatusEventType value
     */
    public static SubscriptionStatusEventType getEnumValueFromInteger(int value) {
        SubscriptionStatusEventType enumValue = enumMapping.get(value);
        if (enumValue != null) {
            return enumValue;
        }
        throw new RuntimeException("Illegal SubscriptionStatusEventType");
    }

    private static Map<Integer, SubscriptionStatusEventType> buildEnumMapping() {
        return Stream.of(SubscriptionStatusEventType.values())
                .collect(Collectors.toMap(SubscriptionStatusEventType::getValue, Function.identity()));
    }

    private final static Map<Integer, SubscriptionStatusEventType> enumMapping = buildEnumMapping();
}
