package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class ListBucketsOutput {
  private List<Bucket> buckets;

  private Owner owner;

  public ListBucketsOutput() {
    this.buckets = null;
    this.owner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ListBucketsOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof ListBucketsOutput);
  }

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
