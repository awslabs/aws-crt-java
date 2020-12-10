package com.amazonaws.s3;

import java.lang.Integer;
import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ListMultipartUploadsRequest {
  private String bucket;

  private String delimiter;

  private EncodingType encodingType;

  private String keyMarker;

  private Integer maxUploads;

  private String prefix;

  private String uploadIdMarker;

  private String expectedBucketOwner;

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
  public EncodingType getEncodingType() {
    return encodingType;
  }

  /**
   * <p>Requests Amazon S3 to encode the object keys in the response and specifies the encoding
   *          method to use. An object key may contain any Unicode character; however, XML 1.0 parser
   *          cannot parse some characters, such as characters with an ASCII value from 0 to 10. For
   *          characters that are not supported in XML 1.0, you can add this parameter to request that
   *          Amazon S3 encode the keys in the response.</p>
   */
  public void setEncodingType(final EncodingType encodingType) {
    this.encodingType = encodingType;
  }

  public String getKeyMarker() {
    return keyMarker;
  }

  public void setKeyMarker(final String keyMarker) {
    this.keyMarker = keyMarker;
  }

  public Integer getMaxUploads() {
    return maxUploads;
  }

  public void setMaxUploads(final Integer maxUploads) {
    this.maxUploads = maxUploads;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  public String getUploadIdMarker() {
    return uploadIdMarker;
  }

  public void setUploadIdMarker(final String uploadIdMarker) {
    this.uploadIdMarker = uploadIdMarker;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
