package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class RequestPaymentConfiguration {
  private Payer payer;

  public Payer getPayer() {
    return payer;
  }

  public void setPayer(final Payer payer) {
    this.payer = payer;
  }
}
