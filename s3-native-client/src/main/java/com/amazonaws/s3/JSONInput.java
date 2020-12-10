package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class JSONInput {
  private JSONType type;

  public JSONType getType() {
    return type;
  }

  public void setType(final JSONType type) {
    this.type = type;
  }
}
