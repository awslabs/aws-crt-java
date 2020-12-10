package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class IndexDocument {
  private String suffix;

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(final String suffix) {
    this.suffix = suffix;
  }
}
