package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class ListObjectsV2Request {
  private String bucket;

  private String delimiter;

  private EncodingType encodingType;

  private Integer maxKeys;

  private String prefix;

  private String continuationToken;

  private Boolean fetchOwner;

  private String startAfter;

  private RequestPayer requestPayer;

  private String expectedBucketOwner;

  public ListObjectsV2Request() {
    this.bucket = null;
    this.delimiter = null;
    this.encodingType = null;
    this.maxKeys = null;
    this.prefix = null;
    this.continuationToken = null;
    this.fetchOwner = null;
    this.startAfter = null;
    this.requestPayer = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ListObjectsV2Request.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof ListObjectsV2Request);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(final String delimiter) {
    this.delimiter = delimiter;
  }

  /**
   * <p>Requests Amazon S3 to encode the object keys in the response and specifies the encoding
   *          method to use. An object key may contain any Unicode character; however, XML 1.0 parser
   *          cannot parse some characters, such as characters with an ASCII value from 0 to 10. For
   *          characters that are not supported in XML 1.0, you can add this parameter to request that
   *          Amazon S3 encode the keys in the response.</p>
   */
  public String getEncodingType() {
    return encodingType;
  }

  /**
   * <p>Requests Amazon S3 to encode the object keys in the response and specifies the encoding
   *          method to use. An object key may contain any Unicode character; however, XML 1.0 parser
   *          cannot parse some characters, such as characters with an ASCII value from 0 to 10. For
   *          characters that are not supported in XML 1.0, you can add this parameter to request that
   *          Amazon S3 encode the keys in the response.</p>
   */
  public void setEncodingType(final String encodingType) {
    this.encodingType = encodingType;
  }

  public Integer getMaxKeys() {
    return maxKeys;
  }

  public void setMaxKeys(final Integer maxKeys) {
    this.maxKeys = maxKeys;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  public String getContinuationToken() {
    return continuationToken;
  }

  public void setContinuationToken(final String continuationToken) {
    this.continuationToken = continuationToken;
  }

  public Boolean isFetchOwner() {
    return fetchOwner;
  }

  public void setFetchOwner(final Boolean fetchOwner) {
    this.fetchOwner = fetchOwner;
  }

  public String getStartAfter() {
    return startAfter;
  }

  public void setStartAfter(final String startAfter) {
    this.startAfter = startAfter;
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
