package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketAccelerateConfigurationOutput {
  private BucketAccelerateStatus status;

  public BucketAccelerateStatus getStatus() {
    return status;
  }

  public void setStatus(final BucketAccelerateStatus status) {
    this.status = status;
  }
}
