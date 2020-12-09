package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketLocationOutput {
  private BucketLocationConstraint locationConstraint;

  public GetBucketLocationOutput() {
    this.locationConstraint = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketLocationOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketLocationOutput);
  }

  public String getLocationConstraint() {
    return locationConstraint;
  }

  public void setLocationConstraint(final String locationConstraint) {
    this.locationConstraint = locationConstraint;
  }
}
