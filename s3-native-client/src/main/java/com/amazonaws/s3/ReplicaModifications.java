package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ReplicaModifications {
  private ReplicaModificationsStatus status;

  public ReplicaModificationsStatus getStatus() {
    return status;
  }

  public void setStatus(final ReplicaModificationsStatus status) {
    this.status = status;
  }
}
