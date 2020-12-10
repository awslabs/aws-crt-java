package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetObjectLegalHoldOutput {
  private ObjectLockLegalHold legalHold;

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
