package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class SourceSelectionCriteria {
  private SseKmsEncryptedObjects sseKmsEncryptedObjects;

  private ReplicaModifications replicaModifications;

  /**
   * <p>A container for filter information for the selection of S3 objects encrypted with AWS
   *          KMS.</p>
   */
  public SseKmsEncryptedObjects getSseKmsEncryptedObjects() {
    return sseKmsEncryptedObjects;
  }

  /**
   * <p>A container for filter information for the selection of S3 objects encrypted with AWS
   *          KMS.</p>
   */
  public void setSseKmsEncryptedObjects(final SseKmsEncryptedObjects sseKmsEncryptedObjects) {
    this.sseKmsEncryptedObjects = sseKmsEncryptedObjects;
  }

  /**
   * <p>A filter that you can specify for selection for modifications on replicas. Amazon S3 doesn't
   *          replicate replica modifications by default. In the latest version of replication
   *          configuration (when <code>Filter</code> is specified), you can specify this element and set
   *          the status to <code>Enabled</code> to replicate modifications on replicas. </p>
   *          <note>
   *             <p> If you don't specify the <code>Filter</code> element, Amazon S3 assumes that the
   *             replication configuration is the earlier version, V1. In the earlier version, this
   *             element is not allowed.</p>
   *          </note>
   */
  public ReplicaModifications getReplicaModifications() {
    return replicaModifications;
  }

  /**
   * <p>A filter that you can specify for selection for modifications on replicas. Amazon S3 doesn't
   *          replicate replica modifications by default. In the latest version of replication
   *          configuration (when <code>Filter</code> is specified), you can specify this element and set
   *          the status to <code>Enabled</code> to replicate modifications on replicas. </p>
   *          <note>
   *             <p> If you don't specify the <code>Filter</code> element, Amazon S3 assumes that the
   *             replication configuration is the earlier version, V1. In the earlier version, this
   *             element is not allowed.</p>
   *          </note>
   */
  public void setReplicaModifications(final ReplicaModifications replicaModifications) {
    this.replicaModifications = replicaModifications;
  }
}
