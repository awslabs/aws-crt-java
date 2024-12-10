/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

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

    private SubscriptionStatusEventType(int value) {
        type = value;
    }
}
