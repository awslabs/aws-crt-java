package com.amazonaws.s3;

import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class LifecycleRule {
  private LifecycleExpiration expiration;

  private String iD;

  private String prefix;

  private LifecycleRuleFilter filter;

  private ExpirationStatus status;

  private List<Transition> transitions;

  private List<NoncurrentVersionTransition> noncurrentVersionTransitions;

  private NoncurrentVersionExpiration noncurrentVersionExpiration;

  private AbortIncompleteMultipartUpload abortIncompleteMultipartUpload;

  /**
   * <p>Container for the expiration for the lifecycle of the object.</p>
   */
  public LifecycleExpiration getExpiration() {
    return expiration;
  }

  /**
   * <p>Container for the expiration for the lifecycle of the object.</p>
   */
  public void setExpiration(final LifecycleExpiration expiration) {
    this.expiration = expiration;
  }

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  /**
   * <p>The <code>Filter</code> is used to identify objects that a Lifecycle Rule applies to. A
   *             <code>Filter</code> must have exactly one of <code>Prefix</code>, <code>Tag</code>, or
   *             <code>And</code> specified.</p>
   */
  public LifecycleRuleFilter getFilter() {
    return filter;
  }

  /**
   * <p>The <code>Filter</code> is used to identify objects that a Lifecycle Rule applies to. A
   *             <code>Filter</code> must have exactly one of <code>Prefix</code>, <code>Tag</code>, or
   *             <code>And</code> specified.</p>
   */
  public void setFilter(final LifecycleRuleFilter filter) {
    this.filter = filter;
  }

  public ExpirationStatus getStatus() {
    return status;
  }

  public void setStatus(final ExpirationStatus status) {
    this.status = status;
  }

  public List<Transition> getTransitions() {
    return transitions;
  }

  public void setTransitions(final List<Transition> transitions) {
    this.transitions = transitions;
  }

  public List<NoncurrentVersionTransition> getNoncurrentVersionTransitions() {
    return noncurrentVersionTransitions;
  }

  public void setNoncurrentVersionTransitions(
      final List<NoncurrentVersionTransition> noncurrentVersionTransitions) {
    this.noncurrentVersionTransitions = noncurrentVersionTransitions;
  }

  /**
   * <p>Specifies when noncurrent object versions expire. Upon expiration, Amazon S3 permanently
   *          deletes the noncurrent object versions. You set this lifecycle configuration action on a
   *          bucket that has versioning enabled (or suspended) to request that Amazon S3 delete noncurrent
   *          object versions at a specific period in the object's lifetime.</p>
   */
  public NoncurrentVersionExpiration getNoncurrentVersionExpiration() {
    return noncurrentVersionExpiration;
  }

  /**
   * <p>Specifies when noncurrent object versions expire. Upon expiration, Amazon S3 permanently
   *          deletes the noncurrent object versions. You set this lifecycle configuration action on a
   *          bucket that has versioning enabled (or suspended) to request that Amazon S3 delete noncurrent
   *          object versions at a specific period in the object's lifetime.</p>
   */
  public void setNoncurrentVersionExpiration(
      final NoncurrentVersionExpiration noncurrentVersionExpiration) {
    this.noncurrentVersionExpiration = noncurrentVersionExpiration;
  }

  /**
   * <p>Specifies the days since the initiation of an incomplete multipart upload that Amazon S3 will
   *          wait before permanently removing all parts of the upload. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/mpuoverview.html#mpu-abort-incomplete-mpu-lifecycle-config">
   *             Aborting Incomplete Multipart Uploads Using a Bucket Lifecycle Policy</a> in the
   *             <i>Amazon Simple Storage Service Developer Guide</i>.</p>
   */
  public AbortIncompleteMultipartUpload getAbortIncompleteMultipartUpload() {
    return abortIncompleteMultipartUpload;
  }

  /**
   * <p>Specifies the days since the initiation of an incomplete multipart upload that Amazon S3 will
   *          wait before permanently removing all parts of the upload. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/mpuoverview.html#mpu-abort-incomplete-mpu-lifecycle-config">
   *             Aborting Incomplete Multipart Uploads Using a Bucket Lifecycle Policy</a> in the
   *             <i>Amazon Simple Storage Service Developer Guide</i>.</p>
   */
  public void setAbortIncompleteMultipartUpload(
      final AbortIncompleteMultipartUpload abortIncompleteMultipartUpload) {
    this.abortIncompleteMultipartUpload = abortIncompleteMultipartUpload;
  }
}
