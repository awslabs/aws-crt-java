package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ExistingObjectReplication {
  private ExistingObjectReplicationStatus status;

  public ExistingObjectReplicationStatus getStatus() {
    return status;
  }

  public void setStatus(final ExistingObjectReplicationStatus status) {
    this.status = status;
  }
}
