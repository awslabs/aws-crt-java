package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class DeleteObjectsRequest {
  private String bucket;

  private Delete delete;

  private String mFA;

  private RequestPayer requestPayer;

  private Boolean bypassGovernanceRetention;

  private String expectedBucketOwner;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  /**
   * <p>Container for the objects to delete.</p>
   */
  public Delete getDelete() {
    return delete;
  }

  /**
   * <p>Container for the objects to delete.</p>
   */
  public void setDelete(final Delete delete) {
    this.delete = delete;
  }

  public String getMFA() {
    return mFA;
  }

  public void setMFA(final String mFA) {
    this.mFA = mFA;
  }

  /**
   * <p>Confirms that the requester knows that they will be charged for the request. Bucket
   *          owners need not specify this parameter in their requests. For information about downloading
   *          objects from requester pays buckets, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ObjectsinRequesterPaysBuckets.html">Downloading Objects in
   *             Requestor Pays Buckets</a> in the <i>Amazon S3 Developer Guide</i>.</p>
   */
  public RequestPayer getRequestPayer() {
    return requestPayer;
  }

  /**
   * <p>Confirms that the requester knows that they will be charged for the request. Bucket
   *          owners need not specify this parameter in their requests. For information about downloading
   *          objects from requester pays buckets, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ObjectsinRequesterPaysBuckets.html">Downloading Objects in
   *             Requestor Pays Buckets</a> in the <i>Amazon S3 Developer Guide</i>.</p>
   */
  public void setRequestPayer(final RequestPayer requestPayer) {
    this.requestPayer = requestPayer;
  }

  public Boolean isBypassGovernanceRetention() {
    return bypassGovernanceRetention;
  }

  public void setBypassGovernanceRetention(final Boolean bypassGovernanceRetention) {
    this.bypassGovernanceRetention = bypassGovernanceRetention;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
