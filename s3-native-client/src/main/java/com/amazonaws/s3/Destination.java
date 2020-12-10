package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Destination {
  private String bucket;

  private String account;

  private StorageClass storageClass;

  private AccessControlTranslation accessControlTranslation;

  private EncryptionConfiguration encryptionConfiguration;

  private ReplicationTime replicationTime;

  private Metrics metrics;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(final String account) {
    this.account = account;
  }

  public StorageClass getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final StorageClass storageClass) {
    this.storageClass = storageClass;
  }

  /**
   * <p>A container for information about access control for replicas.</p>
   */
  public AccessControlTranslation getAccessControlTranslation() {
    return accessControlTranslation;
  }

  /**
   * <p>A container for information about access control for replicas.</p>
   */
  public void setAccessControlTranslation(final AccessControlTranslation accessControlTranslation) {
    this.accessControlTranslation = accessControlTranslation;
  }

  /**
   * <p>Specifies encryption-related information for an Amazon S3 bucket that is a destination for
   *          replicated objects.</p>
   */
  public EncryptionConfiguration getEncryptionConfiguration() {
    return encryptionConfiguration;
  }

  /**
   * <p>Specifies encryption-related information for an Amazon S3 bucket that is a destination for
   *          replicated objects.</p>
   */
  public void setEncryptionConfiguration(final EncryptionConfiguration encryptionConfiguration) {
    this.encryptionConfiguration = encryptionConfiguration;
  }

  /**
   * <p> A container specifying S3 Replication Time Control (S3 RTC) related information, including whether S3 RTC is
   *          enabled and the time when all objects and operations on objects must be replicated. Must be
   *          specified together with a <code>Metrics</code> block. </p>
   */
  public ReplicationTime getReplicationTime() {
    return replicationTime;
  }

  /**
   * <p> A container specifying S3 Replication Time Control (S3 RTC) related information, including whether S3 RTC is
   *          enabled and the time when all objects and operations on objects must be replicated. Must be
   *          specified together with a <code>Metrics</code> block. </p>
   */
  public void setReplicationTime(final ReplicationTime replicationTime) {
    this.replicationTime = replicationTime;
  }

  /**
   * <p> A container specifying replication metrics-related settings enabling replication
   *          metrics and events.</p>
   */
  public Metrics getMetrics() {
    return metrics;
  }

  /**
   * <p> A container specifying replication metrics-related settings enabling replication
   *          metrics and events.</p>
   */
  public void setMetrics(final Metrics metrics) {
    this.metrics = metrics;
  }
}
