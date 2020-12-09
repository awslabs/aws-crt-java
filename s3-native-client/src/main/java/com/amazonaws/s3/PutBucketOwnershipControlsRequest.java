package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketOwnershipControlsRequest {
  private String bucket;

  private String contentMD5;

  private String expectedBucketOwner;

  private OwnershipControls ownershipControls;

  public PutBucketOwnershipControlsRequest() {
    this.bucket = null;
    this.contentMD5 = null;
    this.expectedBucketOwner = null;
    this.ownershipControls = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketOwnershipControlsRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketOwnershipControlsRequest);
  }

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
