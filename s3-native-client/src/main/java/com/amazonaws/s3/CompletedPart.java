package com.amazonaws.s3;

import java.lang.Integer;
import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class CompletedPart {
  private String eTag;

  private Integer partNumber;

  public String getETag() {
    return eTag;
  }

  public void setETag(final String eTag) {
    this.eTag = eTag;
  }

  public Integer getPartNumber() {
    return partNumber;
  }

  public void setPartNumber(final Integer partNumber) {
    this.partNumber = partNumber;
  }
}
