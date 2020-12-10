package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class SseKmsEncryptedObjects {
  private SseKmsEncryptedObjectsStatus status;

  public SseKmsEncryptedObjectsStatus getStatus() {
    return status;
  }

  public void setStatus(final SseKmsEncryptedObjectsStatus status) {
    this.status = status;
  }
}
