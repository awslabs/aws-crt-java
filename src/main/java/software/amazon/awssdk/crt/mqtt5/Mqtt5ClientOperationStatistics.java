/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

/**
 * Simple statistics about the current state of the client's queue of operations
 */
public class Mqtt5ClientOperationStatistics {

    private long incompleteOperationCount;
    private long incompleteOperationSize;
    private long unackedOperationCount;
    private long unackedOperationSize;

    public Mqtt5ClientOperationStatistics() {}

    /**
     * Returns the total number of operations submitted to the client that have not yet been completed.
     * Note: Unacked operations are a subset of this.
     * @return Total number of operations submitted to the client that have not yet been completed
     */
    public long getIncompleteOperationCount() {
        return incompleteOperationCount;
    }

    /**
     * Returns the total packet size of operations submitted to the client that have not yet been completed.
     * Note: Unacked operations are a subset of this.
     * @return Total packet size of operations submitted to the client that have not yet been completed
     */
    public long getIncompleteOperationSize() {
        return incompleteOperationSize;
    }

    /**
     * Returns the total number of operations that have been sent and are waiting for a corresponding ACK before
     * they can be completed.
     * @return Total number of operations that have been sent and are waiting for a corresponding ACK
     */
    public long getUnackedOperationCount() {
        return unackedOperationCount;
    }

    /**
     * Returns the total packet size of operations that have been sent and are waiting for a corresponding ACK before
     * they can be completed.
     * @return Total packet size of operations that have been sent and are waiting for a corresponding ACK
     */
    public long getUnackedOperationSize() {
        return unackedOperationSize;
    }
}
