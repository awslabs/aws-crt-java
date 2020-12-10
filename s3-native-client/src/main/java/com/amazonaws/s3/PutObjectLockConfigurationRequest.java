package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutObjectLockConfigurationRequest {
  private String bucket;

  private ObjectLockConfiguration objectLockConfiguration;

  private RequestPayer requestPayer;

  private String token;

  private String contentMD5;

  private String expectedBucketOwner;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  /**
   * <p>The container element for Object Lock configuration parameters.</p>
   */
  public ObjectLockConfiguration getObjectLockConfiguration() {
    return objectLockConfiguration;
  }

  /**
   * <p>The container element for Object Lock configuration parameters.</p>
   */
  public void setObjectLockConfiguration(final ObjectLockConfiguration objectLockConfiguration) {
    this.objectLockConfiguration = objectLockConfiguration;
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

  public String getToken() {
    return token;
  }

  public void setToken(final String token) {
    this.token = token;
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
}
