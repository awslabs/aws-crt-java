package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetObjectRetentionOutput {
  private ObjectLockRetention retention;

  public GetObjectRetentionOutput() {
    this.retention = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetObjectRetentionOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetObjectRetentionOutput);
  }

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
