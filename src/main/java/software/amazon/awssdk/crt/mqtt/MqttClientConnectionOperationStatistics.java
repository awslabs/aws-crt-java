package software.amazon.awssdk.crt.mqtt;

/**
 * Simple statistics about the current state of the connection's queue of operations
 */
public class MqttClientConnectionOperationStatistics {

    private long incompleteOperationCount;
    private long incompleteOperationSize;
    private long unackedOperationCount;
    private long unackedOperationSize;

    public MqttClientConnectionOperationStatistics() {}

    /**
     * Returns the total number of operations submitted to the connection that have not yet been completed.
     * Note: Unacked operations are a subset of this.
     * @return Total number of operations submitted to the connection that have not yet been completed
     */
    public long getIncompleteOperationCount() {
        return incompleteOperationCount;
    }

    /**
     * Returns the total packet size of operations submitted to the connection that have not yet been completed.
     * Note: Unacked operations are a subset of this.
     * @return Total packet size of operations submitted to the connection that have not yet been completed
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
