package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketVersioningOutput {
  private BucketVersioningStatus status;

  private MFADeleteStatus mFADelete;

  public BucketVersioningStatus getStatus() {
    return status;
  }

  public void setStatus(final BucketVersioningStatus status) {
    this.status = status;
  }

  public MFADeleteStatus getMFADelete() {
    return mFADelete;
  }

  public void setMFADelete(final MFADeleteStatus mFADelete) {
    this.mFADelete = mFADelete;
  }
}
