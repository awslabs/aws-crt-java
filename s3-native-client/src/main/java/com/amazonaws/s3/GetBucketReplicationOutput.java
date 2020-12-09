package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketReplicationOutput {
  private ReplicationConfiguration replicationConfiguration;

  public GetBucketReplicationOutput() {
    this.replicationConfiguration = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketReplicationOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketReplicationOutput);
  }

  /**
   * <p>A container for replication rules. You can add up to 1,000 rules. The maximum size of a
   *          replication configuration is 2 MB.</p>
   */
  public ReplicationConfiguration getReplicationConfiguration() {
    return replicationConfiguration;
  }

  /**
   * <p>A container for replication rules. You can add up to 1,000 rules. The maximum size of a
   *          replication configuration is 2 MB.</p>
   */
  public void setReplicationConfiguration(final ReplicationConfiguration replicationConfiguration) {
    this.replicationConfiguration = replicationConfiguration;
  }
}
