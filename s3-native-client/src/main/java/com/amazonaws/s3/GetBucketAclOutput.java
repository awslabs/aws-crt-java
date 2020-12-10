package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketAclOutput {
  private Owner owner;

  private List<Grant> grants;

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
