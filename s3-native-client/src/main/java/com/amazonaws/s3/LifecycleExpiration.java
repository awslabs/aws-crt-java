package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Integer;
import java.time.Instant;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class LifecycleExpiration {
  private Instant date;

  private Integer days;

  private Boolean expiredObjectDeleteMarker;

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

  public Boolean isExpiredObjectDeleteMarker() {
    return expiredObjectDeleteMarker;
  }

  public void setExpiredObjectDeleteMarker(final Boolean expiredObjectDeleteMarker) {
    this.expiredObjectDeleteMarker = expiredObjectDeleteMarker;
  }
}
