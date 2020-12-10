package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ReplicationTime {
  private ReplicationTimeStatus status;

  private ReplicationTimeValue time;

  public ReplicationTimeStatus getStatus() {
    return status;
  }

  public void setStatus(final ReplicationTimeStatus status) {
    this.status = status;
  }

  /**
   * <p> A container specifying the time value for S3 Replication Time Control (S3 RTC) and replication metrics
   *             <code>EventThreshold</code>. </p>
   */
  public ReplicationTimeValue getTime() {
    return time;
  }

  /**
   * <p> A container specifying the time value for S3 Replication Time Control (S3 RTC) and replication metrics
   *             <code>EventThreshold</code>. </p>
   */
  public void setTime(final ReplicationTimeValue time) {
    this.time = time;
  }
}
