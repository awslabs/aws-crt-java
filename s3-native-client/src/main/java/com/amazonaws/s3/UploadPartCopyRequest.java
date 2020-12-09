package com.amazonaws.s3;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class UploadPartCopyRequest {
  private String bucket;

  private String copySource;

  private String copySourceIfMatch;

  private Instant copySourceIfModifiedSince;

  private String copySourceIfNoneMatch;

  private Instant copySourceIfUnmodifiedSince;

  private String copySourceRange;

  private String key;

  private Integer partNumber;

  private String uploadId;

  private String sSECustomerAlgorithm;

  private String sSECustomerKey;

  private String sSECustomerKeyMD5;

  private String copySourceSSECustomerAlgorithm;

  private String copySourceSSECustomerKey;

  private String copySourceSSECustomerKeyMD5;

  private RequestPayer requestPayer;

  private String expectedBucketOwner;

  private String expectedSourceBucketOwner;

  public UploadPartCopyRequest() {
    this.bucket = null;
    this.copySource = null;
    this.copySourceIfMatch = null;
    this.copySourceIfModifiedSince = null;
    this.copySourceIfNoneMatch = null;
    this.copySourceIfUnmodifiedSince = null;
    this.copySourceRange = null;
    this.key = null;
    this.partNumber = null;
    this.uploadId = null;
    this.sSECustomerAlgorithm = null;
    this.sSECustomerKey = null;
    this.sSECustomerKeyMD5 = null;
    this.copySourceSSECustomerAlgorithm = null;
    this.copySourceSSECustomerKey = null;
    this.copySourceSSECustomerKeyMD5 = null;
    this.requestPayer = null;
    this.expectedBucketOwner = null;
    this.expectedSourceBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(UploadPartCopyRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof UploadPartCopyRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getCopySource() {
    return copySource;
  }

  public void setCopySource(final String copySource) {
    this.copySource = copySource;
  }

  public String getCopySourceIfMatch() {
    return copySourceIfMatch;
  }

  public void setCopySourceIfMatch(final String copySourceIfMatch) {
    this.copySourceIfMatch = copySourceIfMatch;
  }

  public Instant getCopySourceIfModifiedSince() {
    return copySourceIfModifiedSince;
  }

  public void setCopySourceIfModifiedSince(final Instant copySourceIfModifiedSince) {
    this.copySourceIfModifiedSince = copySourceIfModifiedSince;
  }

  public String getCopySourceIfNoneMatch() {
    return copySourceIfNoneMatch;
  }

  public void setCopySourceIfNoneMatch(final String copySourceIfNoneMatch) {
    this.copySourceIfNoneMatch = copySourceIfNoneMatch;
  }

  public Instant getCopySourceIfUnmodifiedSince() {
    return copySourceIfUnmodifiedSince;
  }

  public void setCopySourceIfUnmodifiedSince(final Instant copySourceIfUnmodifiedSince) {
    this.copySourceIfUnmodifiedSince = copySourceIfUnmodifiedSince;
  }

  public String getCopySourceRange() {
    return copySourceRange;
  }

  public void setCopySourceRange(final String copySourceRange) {
    this.copySourceRange = copySourceRange;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public Integer getPartNumber() {
    return partNumber;
  }

  public void setPartNumber(final Integer partNumber) {
    this.partNumber = partNumber;
  }

  public String getUploadId() {
    return uploadId;
  }

  public void setUploadId(final String uploadId) {
    this.uploadId = uploadId;
  }

  public String getSSECustomerAlgorithm() {
    return sSECustomerAlgorithm;
  }

  public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
    this.sSECustomerAlgorithm = sSECustomerAlgorithm;
  }

  public String getSSECustomerKey() {
    return sSECustomerKey;
  }

  public void setSSECustomerKey(final String sSECustomerKey) {
    this.sSECustomerKey = sSECustomerKey;
  }

  public String getSSECustomerKeyMD5() {
    return sSECustomerKeyMD5;
  }

  public void setSSECustomerKeyMD5(final String sSECustomerKeyMD5) {
    this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
  }

  public String getCopySourceSSECustomerAlgorithm() {
    return copySourceSSECustomerAlgorithm;
  }

  public void setCopySourceSSECustomerAlgorithm(final String copySourceSSECustomerAlgorithm) {
    this.copySourceSSECustomerAlgorithm = copySourceSSECustomerAlgorithm;
  }

  public String getCopySourceSSECustomerKey() {
    return copySourceSSECustomerKey;
  }

  public void setCopySourceSSECustomerKey(final String copySourceSSECustomerKey) {
    this.copySourceSSECustomerKey = copySourceSSECustomerKey;
  }

  public String getCopySourceSSECustomerKeyMD5() {
    return copySourceSSECustomerKeyMD5;
  }

  public void setCopySourceSSECustomerKeyMD5(final String copySourceSSECustomerKeyMD5) {
    this.copySourceSSECustomerKeyMD5 = copySourceSSECustomerKeyMD5;
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

  public String getExpectedSourceBucketOwner() {
    return expectedSourceBucketOwner;
  }

  public void setExpectedSourceBucketOwner(final String expectedSourceBucketOwner) {
    this.expectedSourceBucketOwner = expectedSourceBucketOwner;
  }
}
