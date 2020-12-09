package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetPublicAccessBlockOutput {
  private PublicAccessBlockConfiguration publicAccessBlockConfiguration;

  public GetPublicAccessBlockOutput() {
    this.publicAccessBlockConfiguration = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetPublicAccessBlockOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetPublicAccessBlockOutput);
  }

  /**
   * <p>The PublicAccessBlock configuration that you want to apply to this Amazon S3 bucket. You can
   *          enable the configuration options in any combination. For more information about when Amazon S3
   *          considers a bucket or object public, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/access-control-block-public-access.html#access-control-block-public-access-policy-status">The Meaning of "Public"</a> in the <i>Amazon Simple Storage Service Developer Guide</i>. </p>
   */
  public PublicAccessBlockConfiguration getPublicAccessBlockConfiguration() {
    return publicAccessBlockConfiguration;
  }

  /**
   * <p>The PublicAccessBlock configuration that you want to apply to this Amazon S3 bucket. You can
   *          enable the configuration options in any combination. For more information about when Amazon S3
   *          considers a bucket or object public, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/access-control-block-public-access.html#access-control-block-public-access-policy-status">The Meaning of "Public"</a> in the <i>Amazon Simple Storage Service Developer Guide</i>. </p>
   */
  public void setPublicAccessBlockConfiguration(
      final PublicAccessBlockConfiguration publicAccessBlockConfiguration) {
    this.publicAccessBlockConfiguration = publicAccessBlockConfiguration;
  }
}
