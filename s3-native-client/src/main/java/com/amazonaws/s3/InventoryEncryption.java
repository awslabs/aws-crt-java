package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class InventoryEncryption {
  private SSES3 sSES3;

  private SSEKMS sSEKMS;

  /**
   * <p>Specifies the use of SSE-S3 to encrypt delivered inventory reports.</p>
   */
  public SSES3 getSSES3() {
    return sSES3;
  }

  /**
   * <p>Specifies the use of SSE-S3 to encrypt delivered inventory reports.</p>
   */
  public void setSSES3(final SSES3 sSES3) {
    this.sSES3 = sSES3;
  }

  /**
   * <p>Specifies the use of SSE-KMS to encrypt delivered inventory reports.</p>
   */
  public SSEKMS getSSEKMS() {
    return sSEKMS;
  }

  /**
   * <p>Specifies the use of SSE-KMS to encrypt delivered inventory reports.</p>
   */
  public void setSSEKMS(final SSEKMS sSEKMS) {
    this.sSEKMS = sSEKMS;
  }
}
