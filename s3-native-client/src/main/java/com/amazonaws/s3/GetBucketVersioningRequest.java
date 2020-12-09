package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketVersioningRequest {
  private String bucket;

  private String expectedBucketOwner;

  public GetBucketVersioningRequest() {
    this.bucket = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketVersioningRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketVersioningRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
