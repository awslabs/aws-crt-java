package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ObjectLockLegalHold {
  private ObjectLockLegalHoldStatus status;

  public ObjectLockLegalHoldStatus getStatus() {
    return status;
  }

  public void setStatus(final ObjectLockLegalHoldStatus status) {
    this.status = status;
  }
}
