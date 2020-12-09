package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketRequestPaymentOutput {
  private Payer payer;

  public GetBucketRequestPaymentOutput() {
    this.payer = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketRequestPaymentOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketRequestPaymentOutput);
  }

  public String getPayer() {
    return payer;
  }

  public void setPayer(final String payer) {
    this.payer = payer;
  }
}
