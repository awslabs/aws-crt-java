package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketWebsiteRequest {
  private String bucket;

  private String contentMD5;

  private WebsiteConfiguration websiteConfiguration;

  private String expectedBucketOwner;

  public PutBucketWebsiteRequest() {
    this.bucket = null;
    this.contentMD5 = null;
    this.websiteConfiguration = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketWebsiteRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketWebsiteRequest);
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

  /**
   * <p>Specifies website configuration parameters for an Amazon S3 bucket.</p>
   */
  public WebsiteConfiguration getWebsiteConfiguration() {
    return websiteConfiguration;
  }

  /**
   * <p>Specifies website configuration parameters for an Amazon S3 bucket.</p>
   */
  public void setWebsiteConfiguration(final WebsiteConfiguration websiteConfiguration) {
    this.websiteConfiguration = websiteConfiguration;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
