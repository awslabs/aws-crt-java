package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketVersioningOutput {
  private BucketVersioningStatus status;

  private MFADeleteStatus mFADelete;

  public GetBucketVersioningOutput() {
    this.status = null;
    this.mFADelete = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketVersioningOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketVersioningOutput);
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }

  public String getMFADelete() {
    return mFADelete;
  }

  public void setMFADelete(final String mFADelete) {
    this.mFADelete = mFADelete;
  }
}
