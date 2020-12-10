package com.amazonaws.s3;

import java.lang.Boolean;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PublicAccessBlockConfiguration {
  private Boolean blockPublicAcls;

  private Boolean ignorePublicAcls;

  private Boolean blockPublicPolicy;

  private Boolean restrictPublicBuckets;

  public Boolean isBlockPublicAcls() {
    return blockPublicAcls;
  }

  public void setBlockPublicAcls(final Boolean blockPublicAcls) {
    this.blockPublicAcls = blockPublicAcls;
  }

  public Boolean isIgnorePublicAcls() {
    return ignorePublicAcls;
  }

  public void setIgnorePublicAcls(final Boolean ignorePublicAcls) {
    this.ignorePublicAcls = ignorePublicAcls;
  }

  public Boolean isBlockPublicPolicy() {
    return blockPublicPolicy;
  }

  public void setBlockPublicPolicy(final Boolean blockPublicPolicy) {
    this.blockPublicPolicy = blockPublicPolicy;
  }

  public Boolean isRestrictPublicBuckets() {
    return restrictPublicBuckets;
  }

  public void setRestrictPublicBuckets(final Boolean restrictPublicBuckets) {
    this.restrictPublicBuckets = restrictPublicBuckets;
  }
}
