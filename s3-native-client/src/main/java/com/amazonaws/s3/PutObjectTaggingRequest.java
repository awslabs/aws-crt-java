package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutObjectTaggingRequest {
  private String bucket;

  private String key;

  private String versionId;

  private String contentMD5;

  private Tagging tagging;

  private String expectedBucketOwner;

  public PutObjectTaggingRequest() {
    this.bucket = null;
    this.key = null;
    this.versionId = null;
    this.contentMD5 = null;
    this.tagging = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutObjectTaggingRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutObjectTaggingRequest);
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

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
  }

  public String getContentMD5() {
    return contentMD5;
  }

  public void setContentMD5(final String contentMD5) {
    this.contentMD5 = contentMD5;
  }

  /**
   * <p>Container for <code>TagSet</code> elements.</p>
   */
  public Tagging getTagging() {
    return tagging;
  }

  /**
   * <p>Container for <code>TagSet</code> elements.</p>
   */
  public void setTagging(final Tagging tagging) {
    this.tagging = tagging;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
