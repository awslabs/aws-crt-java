package com.amazonaws.s3;

import java.lang.String;
import java.time.Instant;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class CopyPartResult {
  private String eTag;

  private Instant lastModified;

  public String getETag() {
    return eTag;
  }

  public void setETag(final String eTag) {
    this.eTag = eTag;
  }

  public Instant getLastModified() {
    return lastModified;
  }

  public void setLastModified(final Instant lastModified) {
    this.lastModified = lastModified;
  }
}
