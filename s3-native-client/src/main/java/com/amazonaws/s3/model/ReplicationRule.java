// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
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

    private ReplicationRule() {
        this.iD = null;
        this.priority = null;
        this.prefix = null;
        this.filter = null;
        this.status = null;
        this.sourceSelectionCriteria = null;
        this.existingObjectReplication = null;
        this.destination = null;
        this.deleteMarkerReplication = null;
    }

    private ReplicationRule(Builder builder) {
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

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setID(final String iD) {
        this.iD = iD;
    }

    public Integer priority() {
        return priority;
    }

    public void setPriority(final Integer priority) {
        this.priority = priority;
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public ReplicationRuleFilter filter() {
        return filter;
    }

    public void setFilter(final ReplicationRuleFilter filter) {
        this.filter = filter;
    }

    public ReplicationRuleStatus status() {
        return status;
    }

    public void setStatus(final ReplicationRuleStatus status) {
        this.status = status;
    }

    public SourceSelectionCriteria sourceSelectionCriteria() {
        return sourceSelectionCriteria;
    }

    public void setSourceSelectionCriteria(final SourceSelectionCriteria sourceSelectionCriteria) {
        this.sourceSelectionCriteria = sourceSelectionCriteria;
    }

    public ExistingObjectReplication existingObjectReplication() {
        return existingObjectReplication;
    }

    public void setExistingObjectReplication(
            final ExistingObjectReplication existingObjectReplication) {
        this.existingObjectReplication = existingObjectReplication;
    }

    public Destination destination() {
        return destination;
    }

    public void setDestination(final Destination destination) {
        this.destination = destination;
    }

    public DeleteMarkerReplication deleteMarkerReplication() {
        return deleteMarkerReplication;
    }

    public void setDeleteMarkerReplication(final DeleteMarkerReplication deleteMarkerReplication) {
        this.deleteMarkerReplication = deleteMarkerReplication;
    }

    static final class Builder {
        private String iD;

        private Integer priority;

        private String prefix;

        private ReplicationRuleFilter filter;

        private ReplicationRuleStatus status;

        private SourceSelectionCriteria sourceSelectionCriteria;

        private ExistingObjectReplication existingObjectReplication;

        private Destination destination;

        private DeleteMarkerReplication deleteMarkerReplication;

        private Builder() {
        }

        private Builder(ReplicationRule model) {
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
            return new com.amazonaws.s3.model.ReplicationRule(this);
        }

        /**
         * <p>A unique identifier for the rule. The maximum value is 255 characters.</p>
         */
        public final Builder iD(String iD) {
            this.iD = iD;
            return this;
        }

        /**
         * <p>The priority indicates which rule has precedence whenever two or more replication rules
         *          conflict. Amazon S3 will attempt to replicate objects according to all replication rules.
         *          However, if there are two or more rules with the same destination bucket, then objects will
         *          be replicated according to the rule with the highest priority. The higher the number, the
         *          higher the priority. </p>
         *          <p>For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication.html">Replication</a> in the
         *             <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder priority(Integer priority) {
            this.priority = priority;
            return this;
        }

        /**
         * <p>An object key name prefix that identifies the object or objects to which the rule
         *          applies. The maximum prefix length is 1,024 characters. To include all objects in a bucket,
         *          specify an empty string. </p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder filter(ReplicationRuleFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * <p>Specifies whether the rule is enabled.</p>
         */
        public final Builder status(ReplicationRuleStatus status) {
            this.status = status;
            return this;
        }

        /**
         * <p>A container that describes additional filters for identifying the source objects that
         *          you want to replicate. You can choose to enable or disable the replication of these
         *          objects. Currently, Amazon S3 supports only the filter that you can specify for objects created
         *          with server-side encryption using a customer master key (CMK) stored in AWS Key Management
         *          Service (SSE-KMS).</p>
         */
        public final Builder sourceSelectionCriteria(
                SourceSelectionCriteria sourceSelectionCriteria) {
            this.sourceSelectionCriteria = sourceSelectionCriteria;
            return this;
        }

        /**
         * <p></p>
         */
        public final Builder existingObjectReplication(
                ExistingObjectReplication existingObjectReplication) {
            this.existingObjectReplication = existingObjectReplication;
            return this;
        }

        /**
         * <p>A container for information about the replication destination and its configurations
         *          including enabling the S3 Replication Time Control (S3 RTC).</p>
         */
        public final Builder destination(Destination destination) {
            this.destination = destination;
            return this;
        }

        public final Builder deleteMarkerReplication(
                DeleteMarkerReplication deleteMarkerReplication) {
            this.deleteMarkerReplication = deleteMarkerReplication;
            return this;
        }
    }
}
