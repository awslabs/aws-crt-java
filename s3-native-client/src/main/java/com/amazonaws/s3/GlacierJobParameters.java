package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GlacierJobParameters {
  private Tier tier;

  public Tier getTier() {
    return tier;
  }

  public void setTier(final Tier tier) {
    this.tier = tier;
  }
}
