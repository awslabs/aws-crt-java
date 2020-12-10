package com.amazonaws.s3;

import java.lang.Integer;
import java.time.Instant;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Transition {
  private Instant date;

  private Integer days;

  private TransitionStorageClass storageClass;

  public Instant getDate() {
    return date;
  }

  public void setDate(final Instant date) {
    this.date = date;
  }

  public Integer getDays() {
    return days;
  }

  public void setDays(final Integer days) {
    this.days = days;
  }

  public TransitionStorageClass getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final TransitionStorageClass storageClass) {
    this.storageClass = storageClass;
  }
}
