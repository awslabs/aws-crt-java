package com.amazonaws.s3;

import java.lang.Integer;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class AbortIncompleteMultipartUpload {
  private Integer daysAfterInitiation;

  public Integer getDaysAfterInitiation() {
    return daysAfterInitiation;
  }

  public void setDaysAfterInitiation(final Integer daysAfterInitiation) {
    this.daysAfterInitiation = daysAfterInitiation;
  }
}
