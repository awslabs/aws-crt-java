package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ErrorDocument {
  private String key;

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }
}
