package com.amazonaws.s3;

import java.lang.Integer;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class NoncurrentVersionExpiration {
  private Integer noncurrentDays;

  public Integer getNoncurrentDays() {
    return noncurrentDays;
  }

  public void setNoncurrentDays(final Integer noncurrentDays) {
    this.noncurrentDays = noncurrentDays;
  }
}
