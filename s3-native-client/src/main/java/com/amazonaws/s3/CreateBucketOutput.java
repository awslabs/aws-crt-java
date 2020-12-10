package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class CreateBucketOutput {
  private String location;

  public String getLocation() {
    return location;
  }

  public void setLocation(final String location) {
    this.location = location;
  }
}
