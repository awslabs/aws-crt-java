package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class RecordsEvent {
  private byte[] payload;

  public byte[] getPayload() {
    return payload;
  }

  public void setPayload(final byte[] payload) {
    this.payload = payload;
  }
}
