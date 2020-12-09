package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class CompleteMultipartUploadRequest {
  private String bucket;

  private String key;

  private CompletedMultipartUpload multipartUpload;

  private String uploadId;

  private RequestPayer requestPayer;

  private String expectedBucketOwner;

  public CompleteMultipartUploadRequest() {
    this.bucket = null;
    this.key = null;
    this.multipartUpload = null;
    this.uploadId = null;
    this.requestPayer = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(CompleteMultipartUploadRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof CompleteMultipartUploadRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  /**
   * <p>The container for the completed multipart upload details.</p>
   */
  public CompletedMultipartUpload getMultipartUpload() {
    return multipartUpload;
  }

  /**
   * <p>The container for the completed multipart upload details.</p>
   */
  public void setMultipartUpload(final CompletedMultipartUpload multipartUpload) {
    this.multipartUpload = multipartUpload;
  }

  public String getUploadId() {
    return uploadId;
  }

  public void setUploadId(final String uploadId) {
    this.uploadId = uploadId;
  }

  /**
   * <p>Confirms that the requester knows that they will be charged for the request. Bucket
   *          owners need not specify this parameter in their requests. For information about downloading
   *          objects from requester pays buckets, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ObjectsinRequesterPaysBuckets.html">Downloading Objects in
   *             Requestor Pays Buckets</a> in the <i>Amazon S3 Developer Guide</i>.</p>
   */
  public String getRequestPayer() {
    return requestPayer;
  }

  /**
   * <p>Confirms that the requester knows that they will be charged for the request. Bucket
   *          owners need not specify this parameter in their requests. For information about downloading
   *          objects from requester pays buckets, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ObjectsinRequesterPaysBuckets.html">Downloading Objects in
   *             Requestor Pays Buckets</a> in the <i>Amazon S3 Developer Guide</i>.</p>
   */
  public void setRequestPayer(final String requestPayer) {
    this.requestPayer = requestPayer;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
