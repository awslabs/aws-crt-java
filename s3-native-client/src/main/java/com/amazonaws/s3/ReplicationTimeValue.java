package com.amazonaws.s3;

import java.lang.Integer;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ReplicationTimeValue {
  private Integer minutes;

  public Integer getMinutes() {
    return minutes;
  }

  public void setMinutes(final Integer minutes) {
    this.minutes = minutes;
  }
}
