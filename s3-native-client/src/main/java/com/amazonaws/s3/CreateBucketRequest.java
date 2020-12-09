package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
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

  public CreateBucketRequest() {
    this.aCL = null;
    this.bucket = null;
    this.createBucketConfiguration = null;
    this.grantFullControl = null;
    this.grantRead = null;
    this.grantReadACP = null;
    this.grantWrite = null;
    this.grantWriteACP = null;
    this.objectLockEnabledForBucket = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(CreateBucketRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof CreateBucketRequest);
  }

  public String getACL() {
    return aCL;
  }

  public void setACL(final String aCL) {
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
