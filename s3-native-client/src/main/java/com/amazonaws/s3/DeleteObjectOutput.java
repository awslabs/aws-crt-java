package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class DeleteObjectOutput {
  private Boolean deleteMarker;

  private String versionId;

  private RequestCharged requestCharged;

  public Boolean isDeleteMarker() {
    return deleteMarker;
  }

  public void setDeleteMarker(final Boolean deleteMarker) {
    this.deleteMarker = deleteMarker;
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
  }

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
}
