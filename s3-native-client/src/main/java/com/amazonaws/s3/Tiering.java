package com.amazonaws.s3;

import java.lang.Integer;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Tiering {
  private Integer days;

  private IntelligentTieringAccessTier accessTier;

  public Integer getDays() {
    return days;
  }

  public void setDays(final Integer days) {
    this.days = days;
  }

  public IntelligentTieringAccessTier getAccessTier() {
    return accessTier;
  }

  public void setAccessTier(final IntelligentTieringAccessTier accessTier) {
    this.accessTier = accessTier;
  }
}
