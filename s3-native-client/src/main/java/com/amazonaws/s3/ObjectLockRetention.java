package com.amazonaws.s3;

import java.time.Instant;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ObjectLockRetention {
  private ObjectLockRetentionMode mode;

  private Instant retainUntilDate;

  public ObjectLockRetentionMode getMode() {
    return mode;
  }

  public void setMode(final ObjectLockRetentionMode mode) {
    this.mode = mode;
  }

  public Instant getRetainUntilDate() {
    return retainUntilDate;
  }

  public void setRetainUntilDate(final Instant retainUntilDate) {
    this.retainUntilDate = retainUntilDate;
  }
}
