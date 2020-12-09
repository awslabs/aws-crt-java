package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetObjectAclOutput {
  private Owner owner;

  private List<Grant> grants;

  private RequestCharged requestCharged;

  public GetObjectAclOutput() {
    this.owner = null;
    this.grants = null;
    this.requestCharged = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetObjectAclOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetObjectAclOutput);
  }

  /**
   * <p>Container for the owner's display name and ID.</p>
   */
  public Owner getOwner() {
    return owner;
  }

  /**
   * <p>Container for the owner's display name and ID.</p>
   */
  public void setOwner(final Owner owner) {
    this.owner = owner;
  }

  public List<Grant> getGrants() {
    return grants;
  }

  public void setGrants(final List<Grant> grants) {
    this.grants = grants;
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
