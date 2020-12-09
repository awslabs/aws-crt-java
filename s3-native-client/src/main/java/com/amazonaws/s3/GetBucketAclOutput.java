package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketAclOutput {
  private Owner owner;

  private List<Grant> grants;

  public GetBucketAclOutput() {
    this.owner = null;
    this.grants = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketAclOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketAclOutput);
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
}
