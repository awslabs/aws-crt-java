package com.amazonaws.s3;

import java.lang.Integer;
import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ReplicationRule {
  private String iD;

  private Integer priority;

  private String prefix;

  private ReplicationRuleFilter filter;

  private ReplicationRuleStatus status;

  private SourceSelectionCriteria sourceSelectionCriteria;

  private ExistingObjectReplication existingObjectReplication;

  private Destination destination;

  private DeleteMarkerReplication deleteMarkerReplication;

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(final Integer priority) {
    this.priority = priority;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  /**
   * <p>A filter that identifies the subset of objects to which the replication rule applies. A
   *             <code>Filter</code> must specify exactly one <code>Prefix</code>, <code>Tag</code>, or
   *          an <code>And</code> child element.</p>
   */
  public ReplicationRuleFilter getFilter() {
    return filter;
  }

  /**
   * <p>A filter that identifies the subset of objects to which the replication rule applies. A
   *             <code>Filter</code> must specify exactly one <code>Prefix</code>, <code>Tag</code>, or
   *          an <code>And</code> child element.</p>
   */
  public void setFilter(final ReplicationRuleFilter filter) {
    this.filter = filter;
  }

  public ReplicationRuleStatus getStatus() {
    return status;
  }

  public void setStatus(final ReplicationRuleStatus status) {
    this.status = status;
  }

  /**
   * <p>A container that describes additional filters for identifying the source objects that
   *          you want to replicate. You can choose to enable or disable the replication of these
   *          objects. Currently, Amazon S3 supports only the filter that you can specify for objects created
   *          with server-side encryption using a customer master key (CMK) stored in AWS Key Management
   *          Service (SSE-KMS).</p>
   */
  public SourceSelectionCriteria getSourceSelectionCriteria() {
    return sourceSelectionCriteria;
  }

  /**
   * <p>A container that describes additional filters for identifying the source objects that
   *          you want to replicate. You can choose to enable or disable the replication of these
   *          objects. Currently, Amazon S3 supports only the filter that you can specify for objects created
   *          with server-side encryption using a customer master key (CMK) stored in AWS Key Management
   *          Service (SSE-KMS).</p>
   */
  public void setSourceSelectionCriteria(final SourceSelectionCriteria sourceSelectionCriteria) {
    this.sourceSelectionCriteria = sourceSelectionCriteria;
  }

  /**
   * <p>Optional configuration to replicate existing source bucket objects. For more
   *          information, see <a href=" https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-what-is-isnot-replicated.html#existing-object-replication">Replicating Existing Objects</a> in the <i>Amazon S3 Developer Guide</i>.
   *       </p>
   */
  public ExistingObjectReplication getExistingObjectReplication() {
    return existingObjectReplication;
  }

  /**
   * <p>Optional configuration to replicate existing source bucket objects. For more
   *          information, see <a href=" https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-what-is-isnot-replicated.html#existing-object-replication">Replicating Existing Objects</a> in the <i>Amazon S3 Developer Guide</i>.
   *       </p>
   */
  public void setExistingObjectReplication(
      final ExistingObjectReplication existingObjectReplication) {
    this.existingObjectReplication = existingObjectReplication;
  }

  /**
   * <p>Specifies information about where to publish analysis or configuration results for an
   *          Amazon S3 bucket and S3 Replication Time Control (S3 RTC).</p>
   */
  public Destination getDestination() {
    return destination;
  }

  /**
   * <p>Specifies information about where to publish analysis or configuration results for an
   *          Amazon S3 bucket and S3 Replication Time Control (S3 RTC).</p>
   */
  public void setDestination(final Destination destination) {
    this.destination = destination;
  }

  /**
   * <p>Specifies whether Amazon S3 replicates delete markers. If you specify a <code>Filter</code>
   *          in your replication configuration, you must also include a
   *             <code>DeleteMarkerReplication</code> element. If your <code>Filter</code> includes a
   *             <code>Tag</code> element, the <code>DeleteMarkerReplication</code>
   *             <code>Status</code> must be set to Disabled, because Amazon S3 does not support replicating
   *          delete markers for tag-based rules. For an example configuration, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-add-config.html#replication-config-min-rule-config">Basic Rule Configuration</a>. </p>
   *          <p>For more information about delete marker replication, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/delete-marker-replication.html">Basic Rule
   *             Configuration</a>. </p>
   *          <note>
   *             <p>If you are using an earlier version of the replication configuration, Amazon S3 handles
   *             replication of delete markers differently. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-add-config.html#replication-backward-compat-considerations">Backward Compatibility</a>.</p>
   *          </note>
   */
  public DeleteMarkerReplication getDeleteMarkerReplication() {
    return deleteMarkerReplication;
  }

  /**
   * <p>Specifies whether Amazon S3 replicates delete markers. If you specify a <code>Filter</code>
   *          in your replication configuration, you must also include a
   *             <code>DeleteMarkerReplication</code> element. If your <code>Filter</code> includes a
   *             <code>Tag</code> element, the <code>DeleteMarkerReplication</code>
   *             <code>Status</code> must be set to Disabled, because Amazon S3 does not support replicating
   *          delete markers for tag-based rules. For an example configuration, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-add-config.html#replication-config-min-rule-config">Basic Rule Configuration</a>. </p>
   *          <p>For more information about delete marker replication, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/delete-marker-replication.html">Basic Rule
   *             Configuration</a>. </p>
   *          <note>
   *             <p>If you are using an earlier version of the replication configuration, Amazon S3 handles
   *             replication of delete markers differently. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-add-config.html#replication-backward-compat-considerations">Backward Compatibility</a>.</p>
   *          </note>
   */
  public void setDeleteMarkerReplication(final DeleteMarkerReplication deleteMarkerReplication) {
    this.deleteMarkerReplication = deleteMarkerReplication;
  }
}
