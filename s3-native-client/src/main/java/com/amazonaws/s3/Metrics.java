package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Metrics {
  private MetricsStatus status;

  private ReplicationTimeValue eventThreshold;

  public MetricsStatus getStatus() {
    return status;
  }

  public void setStatus(final MetricsStatus status) {
    this.status = status;
  }

  /**
   * <p> A container specifying the time value for S3 Replication Time Control (S3 RTC) and replication metrics
   *             <code>EventThreshold</code>. </p>
   */
  public ReplicationTimeValue getEventThreshold() {
    return eventThreshold;
  }

  /**
   * <p> A container specifying the time value for S3 Replication Time Control (S3 RTC) and replication metrics
   *             <code>EventThreshold</code>. </p>
   */
  public void setEventThreshold(final ReplicationTimeValue eventThreshold) {
    this.eventThreshold = eventThreshold;
  }
}
