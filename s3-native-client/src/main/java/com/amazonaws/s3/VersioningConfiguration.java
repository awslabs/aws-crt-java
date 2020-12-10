package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class VersioningConfiguration {
  private MFADelete mFADelete;

  private BucketVersioningStatus status;

  public MFADelete getMFADelete() {
    return mFADelete;
  }

  public void setMFADelete(final MFADelete mFADelete) {
    this.mFADelete = mFADelete;
  }

  public BucketVersioningStatus getStatus() {
    return status;
  }

  public void setStatus(final BucketVersioningStatus status) {
    this.status = status;
  }
}
