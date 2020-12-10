package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketOwnershipControlsOutput {
  private OwnershipControls ownershipControls;

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
