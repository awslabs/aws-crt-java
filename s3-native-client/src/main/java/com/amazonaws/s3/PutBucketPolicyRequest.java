package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketPolicyRequest {
  private String bucket;

  private String contentMD5;

  private Boolean confirmRemoveSelfBucketAccess;

  private String policy;

  private String expectedBucketOwner;

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

  public Boolean isConfirmRemoveSelfBucketAccess() {
    return confirmRemoveSelfBucketAccess;
  }

  public void setConfirmRemoveSelfBucketAccess(final Boolean confirmRemoveSelfBucketAccess) {
    this.confirmRemoveSelfBucketAccess = confirmRemoveSelfBucketAccess;
  }

  public String getPolicy() {
    return policy;
  }

  public void setPolicy(final String policy) {
    this.policy = policy;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
