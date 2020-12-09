package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketEncryptionRequest {
  private String bucket;

  private String contentMD5;

  private ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

  private String expectedBucketOwner;

  public PutBucketEncryptionRequest() {
    this.bucket = null;
    this.contentMD5 = null;
    this.serverSideEncryptionConfiguration = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketEncryptionRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketEncryptionRequest);
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
   * <p>Specifies the default server-side-encryption configuration.</p>
   */
  public ServerSideEncryptionConfiguration getServerSideEncryptionConfiguration() {
    return serverSideEncryptionConfiguration;
  }

  /**
   * <p>Specifies the default server-side-encryption configuration.</p>
   */
  public void setServerSideEncryptionConfiguration(
      final ServerSideEncryptionConfiguration serverSideEncryptionConfiguration) {
    this.serverSideEncryptionConfiguration = serverSideEncryptionConfiguration;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
