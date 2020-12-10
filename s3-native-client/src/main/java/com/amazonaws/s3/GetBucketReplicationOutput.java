package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketReplicationOutput {
  private ReplicationConfiguration replicationConfiguration;

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
