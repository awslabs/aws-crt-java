package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetObjectLegalHoldOutput {
  private ObjectLockLegalHold legalHold;

  public GetObjectLegalHoldOutput() {
    this.legalHold = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetObjectLegalHoldOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetObjectLegalHoldOutput);
  }

  /**
   * <p>A Legal Hold configuration for an object.</p>
   */
  public ObjectLockLegalHold getLegalHold() {
    return legalHold;
  }

  /**
   * <p>A Legal Hold configuration for an object.</p>
   */
  public void setLegalHold(final ObjectLockLegalHold legalHold) {
    this.legalHold = legalHold;
  }
}
