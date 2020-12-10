package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketAclRequest {
  private BucketCannedACL aCL;

  private AccessControlPolicy accessControlPolicy;

  private String bucket;

  private String contentMD5;

  private String grantFullControl;

  private String grantRead;

  private String grantReadACP;

  private String grantWrite;

  private String grantWriteACP;

  private String expectedBucketOwner;

  public BucketCannedACL getACL() {
    return aCL;
  }

  public void setACL(final BucketCannedACL aCL) {
    this.aCL = aCL;
  }

  /**
   * <p>Contains the elements that set the ACL permissions for an object per grantee.</p>
   */
  public AccessControlPolicy getAccessControlPolicy() {
    return accessControlPolicy;
  }

  /**
   * <p>Contains the elements that set the ACL permissions for an object per grantee.</p>
   */
  public void setAccessControlPolicy(final AccessControlPolicy accessControlPolicy) {
    this.accessControlPolicy = accessControlPolicy;
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

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
