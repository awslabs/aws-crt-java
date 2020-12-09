package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class RestoreObjectOutput {
  private RequestCharged requestCharged;

  private String restoreOutputPath;

  public RestoreObjectOutput() {
    this.requestCharged = null;
    this.restoreOutputPath = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(RestoreObjectOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof RestoreObjectOutput);
  }

  /**
   * <p>If present, indicates that the requester was successfully charged for the
   *          request.</p>
   */
  public String getRequestCharged() {
    return requestCharged;
  }

  /**
   * <p>If present, indicates that the requester was successfully charged for the
   *          request.</p>
   */
  public void setRequestCharged(final String requestCharged) {
    this.requestCharged = requestCharged;
  }

  public String getRestoreOutputPath() {
    return restoreOutputPath;
  }

  public void setRestoreOutputPath(final String restoreOutputPath) {
    this.restoreOutputPath = restoreOutputPath;
  }
}
