package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.String;
import java.time.Instant;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class DeleteMarkerEntry {
  private Owner owner;

  private String key;

  private String versionId;

  private Boolean isLatest;

  private Instant lastModified;

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

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
  }

  public Boolean isIsLatest() {
    return isLatest;
  }

  public void setIsLatest(final Boolean isLatest) {
    this.isLatest = isLatest;
  }

  public Instant getLastModified() {
    return lastModified;
  }

  public void setLastModified(final Instant lastModified) {
    this.lastModified = lastModified;
  }
}
