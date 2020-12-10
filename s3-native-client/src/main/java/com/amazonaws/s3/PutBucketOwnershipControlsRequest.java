package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketOwnershipControlsRequest {
  private String bucket;

  private String contentMD5;

  private String expectedBucketOwner;

  private OwnershipControls ownershipControls;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getContentMD5() {
    return contentMD5;
  }

  public void setContentMD5(final String contentMD5) {
    this.contentMD5 = contentMD5;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
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
