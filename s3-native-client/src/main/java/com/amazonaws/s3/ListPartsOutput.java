package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.String;
import java.time.Instant;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ListPartsOutput {
  private Instant abortDate;

  private String abortRuleId;

  private String bucket;

  private String key;

  private String uploadId;

  private String partNumberMarker;

  private String nextPartNumberMarker;

  private Integer maxParts;

  private Boolean isTruncated;

  private List<Part> parts;

  private Initiator initiator;

  private Owner owner;

  private StorageClass storageClass;

  private RequestCharged requestCharged;

  public Instant getAbortDate() {
    return abortDate;
  }

  public void setAbortDate(final Instant abortDate) {
    this.abortDate = abortDate;
  }

  public String getAbortRuleId() {
    return abortRuleId;
  }

  public void setAbortRuleId(final String abortRuleId) {
    this.abortRuleId = abortRuleId;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public String getUploadId() {
    return uploadId;
  }

  public void setUploadId(final String uploadId) {
    this.uploadId = uploadId;
  }

  public String getPartNumberMarker() {
    return partNumberMarker;
  }

  public void setPartNumberMarker(final String partNumberMarker) {
    this.partNumberMarker = partNumberMarker;
  }

  public String getNextPartNumberMarker() {
    return nextPartNumberMarker;
  }

  public void setNextPartNumberMarker(final String nextPartNumberMarker) {
    this.nextPartNumberMarker = nextPartNumberMarker;
  }

  public Integer getMaxParts() {
    return maxParts;
  }

  public void setMaxParts(final Integer maxParts) {
    this.maxParts = maxParts;
  }

  public Boolean isIsTruncated() {
    return isTruncated;
  }

  public void setIsTruncated(final Boolean isTruncated) {
    this.isTruncated = isTruncated;
  }

  public List<Part> getParts() {
    return parts;
  }

  public void setParts(final List<Part> parts) {
    this.parts = parts;
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

  public StorageClass getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final StorageClass storageClass) {
    this.storageClass = storageClass;
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
}
