package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class CreateBucketOutput {
  private String location;

  public CreateBucketOutput() {
    this.location = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(CreateBucketOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof CreateBucketOutput);
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(final String location) {
    this.location = location;
  }
}
