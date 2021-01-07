// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ReplicationRule {
    /**
     * <p>A unique identifier for the rule. The maximum value is 255 characters.</p>
     */
    String iD;

    /**
     * <p>The priority indicates which rule has precedence whenever two or more replication rules
     *          conflict. Amazon S3 will attempt to replicate objects according to all replication rules.
     *          However, if there are two or more rules with the same destination bucket, then objects will
     *          be replicated according to the rule with the highest priority. The higher the number, the
     *          higher the priority. </p>
     *          <p>For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication.html">Replication</a> in the
     *             <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    Integer priority;

    /**
     * <p>An object key name prefix that identifies the object or objects to which the rule
     *          applies. The maximum prefix length is 1,024 characters. To include all objects in a bucket,
     *          specify an empty string. </p>
     */
    String prefix;

    ReplicationRuleFilter filter;

    /**
     * <p>Specifies whether the rule is enabled.</p>
     */
    ReplicationRuleStatus status;

    /**
     * <p>A container that describes additional filters for identifying the source objects that
     *          you want to replicate. You can choose to enable or disable the replication of these
     *          objects. Currently, Amazon S3 supports only the filter that you can specify for objects created
     *          with server-side encryption using a customer master key (CMK) stored in AWS Key Management
     *          Service (SSE-KMS).</p>
     */
    SourceSelectionCriteria sourceSelectionCriteria;

    /**
     * <p></p>
     */
    ExistingObjectReplication existingObjectReplication;

    /**
     * <p>A container for information about the replication destination and its configurations
     *          including enabling the S3 Replication Time Control (S3 RTC).</p>
     */
    Destination destination;

    DeleteMarkerReplication deleteMarkerReplication;

    ReplicationRule() {
        this.iD = "";
        this.priority = null;
        this.prefix = "";
        this.filter = null;
        this.status = null;
        this.sourceSelectionCriteria = null;
        this.existingObjectReplication = null;
        this.destination = null;
        this.deleteMarkerReplication = null;
    }

    protected ReplicationRule(BuilderImpl builder) {
        this.iD = builder.iD;
        this.priority = builder.priority;
        this.prefix = builder.prefix;
        this.filter = builder.filter;
        this.status = builder.status;
        this.sourceSelectionCriteria = builder.sourceSelectionCriteria;
        this.existingObjectReplication = builder.existingObjectReplication;
        this.destination = builder.destination;
        this.deleteMarkerReplication = builder.deleteMarkerReplication;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ReplicationRule.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ReplicationRule);
    }

    public String iD() {
        return iD;
    }

    public Integer priority() {
        return priority;
    }

    public String prefix() {
        return prefix;
    }

    public ReplicationRuleFilter filter() {
        return filter;
    }

    public ReplicationRuleStatus status() {
        return status;
    }

    public SourceSelectionCriteria sourceSelectionCriteria() {
        return sourceSelectionCriteria;
    }

    public ExistingObjectReplication existingObjectReplication() {
        return existingObjectReplication;
    }

    public Destination destination() {
        return destination;
    }

    public DeleteMarkerReplication deleteMarkerReplication() {
        return deleteMarkerReplication;
    }

    public void setID(final String iD) {
        this.iD = iD;
    }

    public void setPriority(final Integer priority) {
        this.priority = priority;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setFilter(final ReplicationRuleFilter filter) {
        this.filter = filter;
    }

    public void setStatus(final ReplicationRuleStatus status) {
        this.status = status;
    }

    public void setSourceSelectionCriteria(final SourceSelectionCriteria sourceSelectionCriteria) {
        this.sourceSelectionCriteria = sourceSelectionCriteria;
    }

    public void setExistingObjectReplication(
            final ExistingObjectReplication existingObjectReplication) {
        this.existingObjectReplication = existingObjectReplication;
    }

    public void setDestination(final Destination destination) {
        this.destination = destination;
    }

    public void setDeleteMarkerReplication(final DeleteMarkerReplication deleteMarkerReplication) {
        this.deleteMarkerReplication = deleteMarkerReplication;
    }

    public interface Builder {
        Builder iD(String iD);

        Builder priority(Integer priority);

        Builder prefix(String prefix);

        Builder filter(ReplicationRuleFilter filter);

        Builder status(ReplicationRuleStatus status);

        Builder sourceSelectionCriteria(SourceSelectionCriteria sourceSelectionCriteria);

        Builder existingObjectReplication(ExistingObjectReplication existingObjectReplication);

        Builder destination(Destination destination);

        Builder deleteMarkerReplication(DeleteMarkerReplication deleteMarkerReplication);

        ReplicationRule build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>A unique identifier for the rule. The maximum value is 255 characters.</p>
         */
        String iD;

        /**
         * <p>The priority indicates which rule has precedence whenever two or more replication rules
         *          conflict. Amazon S3 will attempt to replicate objects according to all replication rules.
         *          However, if there are two or more rules with the same destination bucket, then objects will
         *          be replicated according to the rule with the highest priority. The higher the number, the
         *          higher the priority. </p>
         *          <p>For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication.html">Replication</a> in the
         *             <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        Integer priority;

        /**
         * <p>An object key name prefix that identifies the object or objects to which the rule
         *          applies. The maximum prefix length is 1,024 characters. To include all objects in a bucket,
         *          specify an empty string. </p>
         */
        String prefix;

        ReplicationRuleFilter filter;

        /**
         * <p>Specifies whether the rule is enabled.</p>
         */
        ReplicationRuleStatus status;

        /**
         * <p>A container that describes additional filters for identifying the source objects that
         *          you want to replicate. You can choose to enable or disable the replication of these
         *          objects. Currently, Amazon S3 supports only the filter that you can specify for objects created
         *          with server-side encryption using a customer master key (CMK) stored in AWS Key Management
         *          Service (SSE-KMS).</p>
         */
        SourceSelectionCriteria sourceSelectionCriteria;

        /**
         * <p></p>
         */
        ExistingObjectReplication existingObjectReplication;

        /**
         * <p>A container for information about the replication destination and its configurations
         *          including enabling the S3 Replication Time Control (S3 RTC).</p>
         */
        Destination destination;

        DeleteMarkerReplication deleteMarkerReplication;

        protected BuilderImpl() {
        }

        private BuilderImpl(ReplicationRule model) {
            iD(model.iD);
            priority(model.priority);
            prefix(model.prefix);
            filter(model.filter);
            status(model.status);
            sourceSelectionCriteria(model.sourceSelectionCriteria);
            existingObjectReplication(model.existingObjectReplication);
            destination(model.destination);
            deleteMarkerReplication(model.deleteMarkerReplication);
        }

        public ReplicationRule build() {
            return new ReplicationRule(this);
        }

        public final Builder iD(String iD) {
            this.iD = iD;
            return this;
        }

        public final Builder priority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder filter(ReplicationRuleFilter filter) {
            this.filter = filter;
            return this;
        }

        public final Builder status(ReplicationRuleStatus status) {
            this.status = status;
            return this;
        }

        public final Builder sourceSelectionCriteria(
                SourceSelectionCriteria sourceSelectionCriteria) {
            this.sourceSelectionCriteria = sourceSelectionCriteria;
            return this;
        }

        public final Builder existingObjectReplication(
                ExistingObjectReplication existingObjectReplication) {
            this.existingObjectReplication = existingObjectReplication;
            return this;
        }

        public final Builder destination(Destination destination) {
            this.destination = destination;
            return this;
        }

        public final Builder deleteMarkerReplication(
                DeleteMarkerReplication deleteMarkerReplication) {
            this.deleteMarkerReplication = deleteMarkerReplication;
            return this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(BuilderImpl.class);
        }

        @Override
        public boolean equals(Object rhs) {
            if (rhs == null) return false;
            return (rhs instanceof BuilderImpl);
        }

        public String iD() {
            return iD;
        }

        public Integer priority() {
            return priority;
        }

        public String prefix() {
            return prefix;
        }

        public ReplicationRuleFilter filter() {
            return filter;
        }

        public ReplicationRuleStatus status() {
            return status;
        }

        public SourceSelectionCriteria sourceSelectionCriteria() {
            return sourceSelectionCriteria;
        }

        public ExistingObjectReplication existingObjectReplication() {
            return existingObjectReplication;
        }

        public Destination destination() {
            return destination;
        }

        public DeleteMarkerReplication deleteMarkerReplication() {
            return deleteMarkerReplication;
        }

        public void setID(final String iD) {
            this.iD = iD;
        }

        public void setPriority(final Integer priority) {
            this.priority = priority;
        }

        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }

        public void setFilter(final ReplicationRuleFilter filter) {
            this.filter = filter;
        }

        public void setStatus(final ReplicationRuleStatus status) {
            this.status = status;
        }

        public void setSourceSelectionCriteria(
                final SourceSelectionCriteria sourceSelectionCriteria) {
            this.sourceSelectionCriteria = sourceSelectionCriteria;
        }

        public void setExistingObjectReplication(
                final ExistingObjectReplication existingObjectReplication) {
            this.existingObjectReplication = existingObjectReplication;
        }

        public void setDestination(final Destination destination) {
            this.destination = destination;
        }

        public void setDeleteMarkerReplication(
                final DeleteMarkerReplication deleteMarkerReplication) {
            this.deleteMarkerReplication = deleteMarkerReplication;
        }
    }
}
