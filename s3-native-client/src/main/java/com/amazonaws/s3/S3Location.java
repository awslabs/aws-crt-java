package com.amazonaws.s3;

import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class S3Location {
  private String bucketName;

  private String prefix;

  private Encryption encryption;

  private ObjectCannedACL cannedACL;

  private List<Grant> accessControlList;

  private Tagging tagging;

  private List<MetadataEntry> userMetadata;

  private StorageClass storageClass;

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(final String bucketName) {
    this.bucketName = bucketName;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  /**
   * <p>Contains the type of server-side encryption used.</p>
   */
  public Encryption getEncryption() {
    return encryption;
  }

  /**
   * <p>Contains the type of server-side encryption used.</p>
   */
  public void setEncryption(final Encryption encryption) {
    this.encryption = encryption;
  }

  public ObjectCannedACL getCannedACL() {
    return cannedACL;
  }

  public void setCannedACL(final ObjectCannedACL cannedACL) {
    this.cannedACL = cannedACL;
  }

  public List<Grant> getAccessControlList() {
    return accessControlList;
  }

  public void setAccessControlList(final List<Grant> accessControlList) {
    this.accessControlList = accessControlList;
  }

  /**
   * <p>Container for <code>TagSet</code> elements.</p>
   */
  public Tagging getTagging() {
    return tagging;
  }

  /**
   * <p>Container for <code>TagSet</code> elements.</p>
   */
  public void setTagging(final Tagging tagging) {
    this.tagging = tagging;
  }

  public List<MetadataEntry> getUserMetadata() {
    return userMetadata;
  }

  public void setUserMetadata(final List<MetadataEntry> userMetadata) {
    this.userMetadata = userMetadata;
  }

  public StorageClass getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final StorageClass storageClass) {
    this.storageClass = storageClass;
  }
}
