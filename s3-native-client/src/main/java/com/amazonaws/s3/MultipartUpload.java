package com.amazonaws.s3;

import java.lang.String;
import java.time.Instant;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class MultipartUpload {
  private String uploadId;

  private String key;

  private Instant initiated;

  private StorageClass storageClass;

  private Owner owner;

  private Initiator initiator;

  public String getUploadId() {
    return uploadId;
  }

  public void setUploadId(final String uploadId) {
    this.uploadId = uploadId;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public Instant getInitiated() {
    return initiated;
  }

  public void setInitiated(final Instant initiated) {
    this.initiated = initiated;
  }

  public StorageClass getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final StorageClass storageClass) {
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

  /**
   * <p>Container element that identifies who initiated the multipart upload. </p>
   */
  public Initiator getInitiator() {
    return initiator;
  }

  /**
   * <p>Container element that identifies who initiated the multipart upload. </p>
   */
  public void setInitiator(final Initiator initiator) {
    this.initiator = initiator;
  }
}
