package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class DeleteObjectsOutput {
  private List<DeletedObject> deleted;

  private RequestCharged requestCharged;

  private List<Error> errors;

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

  public List<Error> getErrors() {
    return errors;
  }

  public void setErrors(final List<Error> errors) {
    this.errors = errors;
  }
}
