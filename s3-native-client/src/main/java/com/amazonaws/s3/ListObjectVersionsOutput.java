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
public class ListObjectVersionsOutput {
  private Boolean isTruncated;

  private String keyMarker;

  private String versionIdMarker;

  private String nextKeyMarker;

  private String nextVersionIdMarker;

  private List<ObjectVersion> versions;

  private List<DeleteMarkerEntry> deleteMarkers;

  private String name;

  private String prefix;

  private String delimiter;

  private Integer maxKeys;

  private List<CommonPrefix> commonPrefixes;

  private EncodingType encodingType;

  public ListObjectVersionsOutput() {
    this.isTruncated = null;
    this.keyMarker = null;
    this.versionIdMarker = null;
    this.nextKeyMarker = null;
    this.nextVersionIdMarker = null;
    this.versions = null;
    this.deleteMarkers = null;
    this.name = null;
    this.prefix = null;
    this.delimiter = null;
    this.maxKeys = null;
    this.commonPrefixes = null;
    this.encodingType = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ListObjectVersionsOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof ListObjectVersionsOutput);
  }

  public Boolean isIsTruncated() {
    return isTruncated;
  }

  public void setIsTruncated(final Boolean isTruncated) {
    this.isTruncated = isTruncated;
  }

  public String getKeyMarker() {
    return keyMarker;
  }

  public void setKeyMarker(final String keyMarker) {
    this.keyMarker = keyMarker;
  }

  public String getVersionIdMarker() {
    return versionIdMarker;
  }

  public void setVersionIdMarker(final String versionIdMarker) {
    this.versionIdMarker = versionIdMarker;
  }

  public String getNextKeyMarker() {
    return nextKeyMarker;
  }

  public void setNextKeyMarker(final String nextKeyMarker) {
    this.nextKeyMarker = nextKeyMarker;
  }

  public String getNextVersionIdMarker() {
    return nextVersionIdMarker;
  }

  public void setNextVersionIdMarker(final String nextVersionIdMarker) {
    this.nextVersionIdMarker = nextVersionIdMarker;
  }

  public List<ObjectVersion> getVersions() {
    return versions;
  }

  public void setVersions(final List<ObjectVersion> versions) {
    this.versions = versions;
  }

  public List<DeleteMarkerEntry> getDeleteMarkers() {
    return deleteMarkers;
  }

  public void setDeleteMarkers(final List<DeleteMarkerEntry> deleteMarkers) {
    this.deleteMarkers = deleteMarkers;
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
