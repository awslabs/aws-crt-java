package com.amazonaws.s3;

import java.lang.Integer;
import java.lang.String;
import java.time.Instant;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Part {
  private Integer partNumber;

  private Instant lastModified;

  private String eTag;

  private Integer size;

  public Integer getPartNumber() {
    return partNumber;
  }

  public void setPartNumber(final Integer partNumber) {
    this.partNumber = partNumber;
  }

  public Instant getLastModified() {
    return lastModified;
  }

  public void setLastModified(final Instant lastModified) {
    this.lastModified = lastModified;
  }

  public String getETag() {
    return eTag;
  }

  public void setETag(final String eTag) {
    this.eTag = eTag;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(final Integer size) {
    this.size = size;
  }
}
