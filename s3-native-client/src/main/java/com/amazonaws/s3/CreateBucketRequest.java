package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class CreateBucketRequest {
  private BucketCannedACL aCL;

  private String bucket;

  private CreateBucketConfiguration createBucketConfiguration;

  private String grantFullControl;

  private String grantRead;

  private String grantReadACP;

  private String grantWrite;

  private String grantWriteACP;

  private Boolean objectLockEnabledForBucket;

  public BucketCannedACL getACL() {
    return aCL;
  }

  public void setACL(final BucketCannedACL aCL) {
    this.aCL = aCL;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  /**
   * <p>The configuration information for the bucket.</p>
   */
  public CreateBucketConfiguration getCreateBucketConfiguration() {
    return createBucketConfiguration;
  }

  /**
   * <p>The configuration information for the bucket.</p>
   */
  public void setCreateBucketConfiguration(
      final CreateBucketConfiguration createBucketConfiguration) {
    this.createBucketConfiguration = createBucketConfiguration;
  }

  public String getGrantFullControl() {
    return grantFullControl;
  }

  public void setGrantFullControl(final String grantFullControl) {
    this.grantFullControl = grantFullControl;
  }

  public String getGrantRead() {
    return grantRead;
  }

  public void setGrantRead(final String grantRead) {
    this.grantRead = grantRead;
  }

  public String getGrantReadACP() {
    return grantReadACP;
  }

  public void setGrantReadACP(final String grantReadACP) {
    this.grantReadACP = grantReadACP;
  }

  public String getGrantWrite() {
    return grantWrite;
  }

  public void setGrantWrite(final String grantWrite) {
    this.grantWrite = grantWrite;
  }

  public String getGrantWriteACP() {
    return grantWriteACP;
  }

  public void setGrantWriteACP(final String grantWriteACP) {
    this.grantWriteACP = grantWriteACP;
  }

  public Boolean isObjectLockEnabledForBucket() {
    return objectLockEnabledForBucket;
  }

  public void setObjectLockEnabledForBucket(final Boolean objectLockEnabledForBucket) {
    this.objectLockEnabledForBucket = objectLockEnabledForBucket;
  }
}
