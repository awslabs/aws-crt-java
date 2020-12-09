package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class ListObjectsOutput {
  private Boolean isTruncated;

  private String marker;

  private String nextMarker;

  private List<Object> contents;

  private String name;

  private String prefix;

  private String delimiter;

  private Integer maxKeys;

  private List<CommonPrefix> commonPrefixes;

  private EncodingType encodingType;

  public ListObjectsOutput() {
    this.isTruncated = null;
    this.marker = null;
    this.nextMarker = null;
    this.contents = null;
    this.name = null;
    this.prefix = null;
    this.delimiter = null;
    this.maxKeys = null;
    this.commonPrefixes = null;
    this.encodingType = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ListObjectsOutput.class);
  }

  @Override
  public boolean equals(java.lang.Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof ListObjectsOutput);
  }

  public Boolean isIsTruncated() {
    return isTruncated;
  }

  public void setIsTruncated(final Boolean isTruncated) {
    this.isTruncated = isTruncated;
  }

  public String getMarker() {
    return marker;
  }

  public void setMarker(final String marker) {
    this.marker = marker;
  }

  public String getNextMarker() {
    return nextMarker;
  }

  public void setNextMarker(final String nextMarker) {
    this.nextMarker = nextMarker;
  }

  public List<Object> getContents() {
    return contents;
  }

  public void setContents(final List<Object> contents) {
    this.contents = contents;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
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

  public Integer getMaxKeys() {
    return maxKeys;
  }

  public void setMaxKeys(final Integer maxKeys) {
    this.maxKeys = maxKeys;
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
