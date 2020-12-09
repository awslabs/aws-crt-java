package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class DeleteObjectsOutput {
  private List<DeletedObject> deleted;

  private RequestCharged requestCharged;

  private List<Error> errors;

  public DeleteObjectsOutput() {
    this.deleted = null;
    this.requestCharged = null;
    this.errors = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(DeleteObjectsOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof DeleteObjectsOutput);
  }

  public List<DeletedObject> getDeleted() {
    return deleted;
  }

  public void setDeleted(final List<DeletedObject> deleted) {
    this.deleted = deleted;
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

  public List<Error> getErrors() {
    return errors;
  }

  public void setErrors(final List<Error> errors) {
    this.errors = errors;
  }
}
