package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ListBucketsOutput {
  private List<Bucket> buckets;

  private Owner owner;

  public List<Bucket> getBuckets() {
    return buckets;
  }

  public void setBuckets(final List<Bucket> buckets) {
    this.buckets = buckets;
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
}
