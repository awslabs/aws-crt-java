package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetObjectRetentionOutput {
  private ObjectLockRetention retention;

  /**
   * <p>A Retention configuration for an object.</p>
   */
  public ObjectLockRetention getRetention() {
    return retention;
  }

  /**
   * <p>A Retention configuration for an object.</p>
   */
  public void setRetention(final ObjectLockRetention retention) {
    this.retention = retention;
  }
}
