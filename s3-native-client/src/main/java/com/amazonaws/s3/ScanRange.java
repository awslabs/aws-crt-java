package com.amazonaws.s3;

import java.lang.Long;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ScanRange {
  private Long start;

  private Long end;

  public Long getStart() {
    return start;
  }

  public void setStart(final Long start) {
    this.start = start;
  }

  public Long getEnd() {
    return end;
  }

  public void setEnd(final Long end) {
    this.end = end;
  }
}
