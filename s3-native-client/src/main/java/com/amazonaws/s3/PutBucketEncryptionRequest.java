package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketEncryptionRequest {
  private String bucket;

  private String contentMD5;

  private ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

  private String expectedBucketOwner;

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
