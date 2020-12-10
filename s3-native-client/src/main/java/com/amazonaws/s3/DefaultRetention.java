package com.amazonaws.s3;

import java.lang.Integer;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class DefaultRetention {
  private ObjectLockRetentionMode mode;

  private Integer days;

  private Integer years;

  public ObjectLockRetentionMode getMode() {
    return mode;
  }

  public void setMode(final ObjectLockRetentionMode mode) {
    this.mode = mode;
  }

  public Integer getDays() {
    return days;
  }

  public void setDays(final Integer days) {
    this.days = days;
  }

  public Integer getYears() {
    return years;
  }

  public void setYears(final Integer years) {
    this.years = years;
  }
}
