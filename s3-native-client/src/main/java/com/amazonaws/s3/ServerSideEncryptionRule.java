package com.amazonaws.s3;

import java.lang.Boolean;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ServerSideEncryptionRule {
  private ServerSideEncryptionByDefault applyServerSideEncryptionByDefault;

  private Boolean bucketKeyEnabled;

  /**
   * <p>Describes the default server-side encryption to apply to new objects in the bucket. If a
   *          PUT Object request doesn't specify any server-side encryption, this default encryption will
   *          be applied. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTencryption.html">PUT Bucket encryption</a> in
   *          the <i>Amazon Simple Storage Service API Reference</i>.</p>
   */
  public ServerSideEncryptionByDefault getApplyServerSideEncryptionByDefault() {
    return applyServerSideEncryptionByDefault;
  }

  /**
   * <p>Describes the default server-side encryption to apply to new objects in the bucket. If a
   *          PUT Object request doesn't specify any server-side encryption, this default encryption will
   *          be applied. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTencryption.html">PUT Bucket encryption</a> in
   *          the <i>Amazon Simple Storage Service API Reference</i>.</p>
   */
  public void setApplyServerSideEncryptionByDefault(
      final ServerSideEncryptionByDefault applyServerSideEncryptionByDefault) {
    this.applyServerSideEncryptionByDefault = applyServerSideEncryptionByDefault;
  }

  public Boolean isBucketKeyEnabled() {
    return bucketKeyEnabled;
  }

  public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
    this.bucketKeyEnabled = bucketKeyEnabled;
  }
}
