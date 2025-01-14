/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

import java.util.Optional;

/**
 * An event that describes a change in subscription status for a streaming operation.
 */
public class SubscriptionStatusEvent {
    private final SubscriptionStatusEventType type;
    private final Optional<Integer> error;

    private SubscriptionStatusEvent(SubscriptionStatusEventType type, int errorCode) {
        this.type = type;
        if (errorCode != 0) {
            this.error = Optional.of(errorCode);
        } else {
            this.error = Optional.empty();
        }
    }

    /**
     * Gets the type of status change represented by the event.
     *
     * @return The type of status change represented by the event
     */
    public SubscriptionStatusEventType getType() {
        return this.type;
    }

    /**
     * Gets the underlying reason for the event.  Only set for SubscriptionLost and SubscriptionHalted.  Use
     * CRT.awsErrorString() to convert the integer error code into an error description.
     *
     * @return  underlying reason for the event
     */
    public Optional<Integer> getError() {
        return this.error;
    }
}
