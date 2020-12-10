package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class DeleteMarkerReplication {
  private DeleteMarkerReplicationStatus status;

  public DeleteMarkerReplicationStatus getStatus() {
    return status;
  }

  public void setStatus(final DeleteMarkerReplicationStatus status) {
    this.status = status;
  }
}
