package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class DeleteObjectOutput {
  private Boolean deleteMarker;

  private String versionId;

  private RequestCharged requestCharged;

  public DeleteObjectOutput() {
    this.deleteMarker = null;
    this.versionId = null;
    this.requestCharged = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(DeleteObjectOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof DeleteObjectOutput);
  }

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
}
