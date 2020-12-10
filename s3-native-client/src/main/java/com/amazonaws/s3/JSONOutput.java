package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class JSONOutput {
  private String recordDelimiter;

  public String getRecordDelimiter() {
    return recordDelimiter;
  }

  public void setRecordDelimiter(final String recordDelimiter) {
    this.recordDelimiter = recordDelimiter;
  }
}
