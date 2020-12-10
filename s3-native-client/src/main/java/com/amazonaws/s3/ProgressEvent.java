package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ProgressEvent {
  private Progress details;

  /**
   * <p>This data type contains information about progress of an operation.</p>
   */
  public Progress getDetails() {
    return details;
  }

  /**
   * <p>This data type contains information about progress of an operation.</p>
   */
  public void setDetails(final Progress details) {
    this.details = details;
  }
}
