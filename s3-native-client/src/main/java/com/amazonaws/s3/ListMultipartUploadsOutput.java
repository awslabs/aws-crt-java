package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class ListMultipartUploadsOutput {
  private String bucket;

  private String keyMarker;

  private String uploadIdMarker;

  private String nextKeyMarker;

  private String prefix;

  private String delimiter;

  private String nextUploadIdMarker;

  private Integer maxUploads;

  private Boolean isTruncated;

  private List<MultipartUpload> uploads;

  private List<CommonPrefix> commonPrefixes;

  private EncodingType encodingType;

  public ListMultipartUploadsOutput() {
    this.bucket = null;
    this.keyMarker = null;
    this.uploadIdMarker = null;
    this.nextKeyMarker = null;
    this.prefix = null;
    this.delimiter = null;
    this.nextUploadIdMarker = null;
    this.maxUploads = null;
    this.isTruncated = null;
    this.uploads = null;
    this.commonPrefixes = null;
    this.encodingType = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ListMultipartUploadsOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof ListMultipartUploadsOutput);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getKeyMarker() {
    return keyMarker;
  }

  public void setKeyMarker(final String keyMarker) {
    this.keyMarker = keyMarker;
  }

  public String getUploadIdMarker() {
    return uploadIdMarker;
  }

  public void setUploadIdMarker(final String uploadIdMarker) {
    this.uploadIdMarker = uploadIdMarker;
  }

  public String getNextKeyMarker() {
    return nextKeyMarker;
  }

  public void setNextKeyMarker(final String nextKeyMarker) {
    this.nextKeyMarker = nextKeyMarker;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(final String delimiter) {
    this.delimiter = delimiter;
  }

  public String getNextUploadIdMarker() {
    return nextUploadIdMarker;
  }

  public void setNextUploadIdMarker(final String nextUploadIdMarker) {
    this.nextUploadIdMarker = nextUploadIdMarker;
  }

  public Integer getMaxUploads() {
    return maxUploads;
  }

  public void setMaxUploads(final Integer maxUploads) {
    this.maxUploads = maxUploads;
  }

  public Boolean isIsTruncated() {
    return isTruncated;
  }

  public void setIsTruncated(final Boolean isTruncated) {
    this.isTruncated = isTruncated;
  }

  public List<MultipartUpload> getUploads() {
    return uploads;
  }

  public void setUploads(final List<MultipartUpload> uploads) {
    this.uploads = uploads;
  }

  public List<CommonPrefix> getCommonPrefixes() {
    return commonPrefixes;
  }

  public void setCommonPrefixes(final List<CommonPrefix> commonPrefixes) {
    this.commonPrefixes = commonPrefixes;
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
}
