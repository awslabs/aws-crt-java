package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketOwnershipControlsOutput {
  private OwnershipControls ownershipControls;

  public GetBucketOwnershipControlsOutput() {
    this.ownershipControls = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketOwnershipControlsOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketOwnershipControlsOutput);
  }

  /**
   * <p>The container element for a bucket's ownership controls.</p>
   */
  public OwnershipControls getOwnershipControls() {
    return ownershipControls;
  }

  /**
   * <p>The container element for a bucket's ownership controls.</p>
   */
  public void setOwnershipControls(final OwnershipControls ownershipControls) {
    this.ownershipControls = ownershipControls;
  }
}
