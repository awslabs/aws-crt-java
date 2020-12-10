package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class RestoreObjectOutput {
  private RequestCharged requestCharged;

  private String restoreOutputPath;

  /**
   * <p>If present, indicates that the requester was successfully charged for the
   *          request.</p>
   */
  public RequestCharged getRequestCharged() {
    return requestCharged;
  }

  /**
   * <p>If present, indicates that the requester was successfully charged for the
   *          request.</p>
   */
  public void setRequestCharged(final RequestCharged requestCharged) {
    this.requestCharged = requestCharged;
  }

  public String getRestoreOutputPath() {
    return restoreOutputPath;
  }

  public void setRestoreOutputPath(final String restoreOutputPath) {
    this.restoreOutputPath = restoreOutputPath;
  }
}
