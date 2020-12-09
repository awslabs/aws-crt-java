package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketVersioningRequest {
  private String bucket;

  private String contentMD5;

  private String mFA;

  private VersioningConfiguration versioningConfiguration;

  private String expectedBucketOwner;

  public PutBucketVersioningRequest() {
    this.bucket = null;
    this.contentMD5 = null;
    this.mFA = null;
    this.versioningConfiguration = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketVersioningRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketVersioningRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getContentMD5() {
    return contentMD5;
  }

  public void setContentMD5(final String contentMD5) {
    this.contentMD5 = contentMD5;
  }

  public String getMFA() {
    return mFA;
  }

  public void setMFA(final String mFA) {
    this.mFA = mFA;
  }

  /**
   * <p>Describes the versioning state of an Amazon S3 bucket. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTVersioningStatus.html">PUT
   *             Bucket versioning</a> in the <i>Amazon Simple Storage Service API Reference</i>.</p>
   */
  public VersioningConfiguration getVersioningConfiguration() {
    return versioningConfiguration;
  }

  /**
   * <p>Describes the versioning state of an Amazon S3 bucket. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTVersioningStatus.html">PUT
   *             Bucket versioning</a> in the <i>Amazon Simple Storage Service API Reference</i>.</p>
   */
  public void setVersioningConfiguration(final VersioningConfiguration versioningConfiguration) {
    this.versioningConfiguration = versioningConfiguration;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
