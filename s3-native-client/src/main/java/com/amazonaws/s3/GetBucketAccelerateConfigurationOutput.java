package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketAccelerateConfigurationOutput {
  private BucketAccelerateStatus status;

  public GetBucketAccelerateConfigurationOutput() {
    this.status = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketAccelerateConfigurationOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketAccelerateConfigurationOutput);
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }
}
