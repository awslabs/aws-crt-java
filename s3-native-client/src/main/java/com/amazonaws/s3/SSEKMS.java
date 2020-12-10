package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class SSEKMS {
  private String keyId;

  public String getKeyId() {
    return keyId;
  }

  public void setKeyId(final String keyId) {
    this.keyId = keyId;
  }
}
