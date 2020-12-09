package com.amazonaws.s3;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetObjectRequest {
  private String bucket;

  private String ifMatch;

  private Instant ifModifiedSince;

  private String ifNoneMatch;

  private Instant ifUnmodifiedSince;

  private String key;

  private String range;

  private String responseCacheControl;

  private String responseContentDisposition;

  private String responseContentEncoding;

  private String responseContentLanguage;

  private String responseContentType;

  private Instant responseExpires;

  private String versionId;

  private String sSECustomerAlgorithm;

  private String sSECustomerKey;

  private String sSECustomerKeyMD5;

  private RequestPayer requestPayer;

  private Integer partNumber;

  private String expectedBucketOwner;

  public GetObjectRequest() {
    this.bucket = null;
    this.ifMatch = null;
    this.ifModifiedSince = null;
    this.ifNoneMatch = null;
    this.ifUnmodifiedSince = null;
    this.key = null;
    this.range = null;
    this.responseCacheControl = null;
    this.responseContentDisposition = null;
    this.responseContentEncoding = null;
    this.responseContentLanguage = null;
    this.responseContentType = null;
    this.responseExpires = null;
    this.versionId = null;
    this.sSECustomerAlgorithm = null;
    this.sSECustomerKey = null;
    this.sSECustomerKeyMD5 = null;
    this.requestPayer = null;
    this.partNumber = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetObjectRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetObjectRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getIfMatch() {
    return ifMatch;
  }

  public void setIfMatch(final String ifMatch) {
    this.ifMatch = ifMatch;
  }

  public Instant getIfModifiedSince() {
    return ifModifiedSince;
  }

  public void setIfModifiedSince(final Instant ifModifiedSince) {
    this.ifModifiedSince = ifModifiedSince;
  }

  public String getIfNoneMatch() {
    return ifNoneMatch;
  }

  public void setIfNoneMatch(final String ifNoneMatch) {
    this.ifNoneMatch = ifNoneMatch;
  }

  public Instant getIfUnmodifiedSince() {
    return ifUnmodifiedSince;
  }

  public void setIfUnmodifiedSince(final Instant ifUnmodifiedSince) {
    this.ifUnmodifiedSince = ifUnmodifiedSince;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public String getRange() {
    return range;
  }

  public void setRange(final String range) {
    this.range = range;
  }

  public String getResponseCacheControl() {
    return responseCacheControl;
  }

  public void setResponseCacheControl(final String responseCacheControl) {
    this.responseCacheControl = responseCacheControl;
  }

  public String getResponseContentDisposition() {
    return responseContentDisposition;
  }

  public void setResponseContentDisposition(final String responseContentDisposition) {
    this.responseContentDisposition = responseContentDisposition;
  }

  public String getResponseContentEncoding() {
    return responseContentEncoding;
  }

  public void setResponseContentEncoding(final String responseContentEncoding) {
    this.responseContentEncoding = responseContentEncoding;
  }

  public String getResponseContentLanguage() {
    return responseContentLanguage;
  }

  public void setResponseContentLanguage(final String responseContentLanguage) {
    this.responseContentLanguage = responseContentLanguage;
  }

  public String getResponseContentType() {
    return responseContentType;
  }

  public void setResponseContentType(final String responseContentType) {
    this.responseContentType = responseContentType;
  }

  public Instant getResponseExpires() {
    return responseExpires;
  }

  public void setResponseExpires(final Instant responseExpires) {
    this.responseExpires = responseExpires;
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
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

  public Integer getPartNumber() {
    return partNumber;
  }

  public void setPartNumber(final Integer partNumber) {
    this.partNumber = partNumber;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
