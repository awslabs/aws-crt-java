package com.amazonaws.s3;

import java.lang.Long;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Stats {
  private Long bytesScanned;

  private Long bytesProcessed;

  private Long bytesReturned;

  public Long getBytesScanned() {
    return bytesScanned;
  }

  public void setBytesScanned(final Long bytesScanned) {
    this.bytesScanned = bytesScanned;
  }

  public Long getBytesProcessed() {
    return bytesProcessed;
  }

  public void setBytesProcessed(final Long bytesProcessed) {
    this.bytesProcessed = bytesProcessed;
  }

  public Long getBytesReturned() {
    return bytesReturned;
  }

  public void setBytesReturned(final Long bytesReturned) {
    this.bytesReturned = bytesReturned;
  }
}
