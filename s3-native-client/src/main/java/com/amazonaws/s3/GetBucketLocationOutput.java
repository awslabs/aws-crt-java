package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketLocationOutput {
  private BucketLocationConstraint locationConstraint;

  public BucketLocationConstraint getLocationConstraint() {
    return locationConstraint;
  }

  public void setLocationConstraint(final BucketLocationConstraint locationConstraint) {
    this.locationConstraint = locationConstraint;
  }
}
