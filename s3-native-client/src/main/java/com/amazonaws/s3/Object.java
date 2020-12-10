package com.amazonaws.s3;

import java.lang.Integer;
import java.lang.String;
import java.time.Instant;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Object {
  private String key;

  private Instant lastModified;

  private String eTag;

  private Integer size;

  private ObjectStorageClass storageClass;

  private Owner owner;

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public Instant getLastModified() {
    return lastModified;
  }

  public void setLastModified(final Instant lastModified) {
    this.lastModified = lastModified;
  }

  public String getETag() {
    return eTag;
  }

  public void setETag(final String eTag) {
    this.eTag = eTag;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(final Integer size) {
    this.size = size;
  }

  public ObjectStorageClass getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final ObjectStorageClass storageClass) {
    this.storageClass = storageClass;
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
