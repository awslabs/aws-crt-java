/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.awssdk.crt.http;

/**
 * Holds tracing metrics for an HTTP stream. Maps to `struct aws_http_stream_metrics` in **aws-c-http**'s
 * **request_response.h**.
 */
public class HttpStreamMetrics {
    private final long sendStartTimestampNs;
    private final long sendEndTimestampNs;
    private final long sendingDurationNs;
    private final long receiveStartTimestampNs;
    private final long receiveEndTimestampNs;
    private final long receivingDurationNs;
    private final int streamId;

    HttpStreamMetrics(
            long sendStartTimestampNs,
            long sendEndTimestampNs,
            long sendingDurationNs,
            long receiveStartTimestampNs,
            long receiveEndTimestampNs,
            long receivingDurationNs,
            int streamId
    ) {
        this.sendStartTimestampNs = sendStartTimestampNs;
        this.sendEndTimestampNs = sendEndTimestampNs;
        this.sendingDurationNs = sendingDurationNs;
        this.receiveStartTimestampNs = receiveStartTimestampNs;
        this.receiveEndTimestampNs = receiveEndTimestampNs;
        this.receivingDurationNs = receivingDurationNs;
        this.streamId = streamId;
    }

    public long getSendStartTimestampNs() {
        return sendStartTimestampNs;
    }

    public long getSendEndTimestampNs() {
        return sendEndTimestampNs;
    }

    public long getSendingDurationNs() {
        return sendingDurationNs;
    }

    public long getReceiveStartTimestampNs() {
        return receiveStartTimestampNs;
    }

    public long getReceiveEndTimestampNs() {
        return receiveEndTimestampNs;
    }

    public long getReceivingDurationNs() {
        return receivingDurationNs;
    }

    public int getStreamId() {
        return streamId;
    }

    @Override
    public String toString() {
        return "HttpStreamMetrics{" +
                "sendStartTimestampNs=" + sendStartTimestampNs +
                ", sendEndTimestampNs=" + sendEndTimestampNs +
                ", sendingDurationNs=" + sendingDurationNs +
                ", receiveStartTimestampNs=" + receiveStartTimestampNs +
                ", receiveEndTimestampNs=" + receiveEndTimestampNs +
                ", receivingDurationNs=" + receivingDurationNs +
                ", streamId=" + streamId +
                '}';
    }
}
