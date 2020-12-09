package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutObjectAclRequest {
  private ObjectCannedACL aCL;

  private AccessControlPolicy accessControlPolicy;

  private String bucket;

  private String contentMD5;

  private String grantFullControl;

  private String grantRead;

  private String grantReadACP;

  private String grantWrite;

  private String grantWriteACP;

  private String key;

  private RequestPayer requestPayer;

  private String versionId;

  private String expectedBucketOwner;

  public PutObjectAclRequest() {
    this.aCL = null;
    this.accessControlPolicy = null;
    this.bucket = null;
    this.contentMD5 = null;
    this.grantFullControl = null;
    this.grantRead = null;
    this.grantReadACP = null;
    this.grantWrite = null;
    this.grantWriteACP = null;
    this.key = null;
    this.requestPayer = null;
    this.versionId = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutObjectAclRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutObjectAclRequest);
  }

  public String getACL() {
    return aCL;
  }

  public void setACL(final String aCL) {
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

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  /**
   * <p>Confirms that the requester knows that they will be charged for the request. Bucket
   *          owners need not specify this parameter in their requests. For information about downloading
   *          objects from requester pays buckets, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ObjectsinRequesterPaysBuckets.html">Downloading Objects in
   *             Requestor Pays Buckets</a> in the <i>Amazon S3 Developer Guide</i>.</p>
   */
  public String getRequestPayer() {
    return requestPayer;
  }

  /**
   * <p>Confirms that the requester knows that they will be charged for the request. Bucket
   *          owners need not specify this parameter in their requests. For information about downloading
   *          objects from requester pays buckets, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ObjectsinRequesterPaysBuckets.html">Downloading Objects in
   *             Requestor Pays Buckets</a> in the <i>Amazon S3 Developer Guide</i>.</p>
   */
  public void setRequestPayer(final String requestPayer) {
    this.requestPayer = requestPayer;
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
