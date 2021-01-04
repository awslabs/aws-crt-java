// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class SourceSelectionCriteria {
    /**
     * <p> A container for filter information for the selection of Amazon S3 objects encrypted with AWS
     *          KMS. If you include <code>SourceSelectionCriteria</code> in the replication configuration,
     *          this element is required. </p>
     */
    SseKmsEncryptedObjects sseKmsEncryptedObjects;

    /**
     * <p>A filter that you can specify for selections for modifications on replicas. Amazon S3 doesn't
     *          replicate replica modifications by default. In the latest version of replication
     *          configuration (when <code>Filter</code> is specified), you can specify this element and set
     *          the status to <code>Enabled</code> to replicate modifications on replicas. </p>
     *          <note>
     *             <p> If you don't specify the <code>Filter</code> element, Amazon S3 assumes that the
     *             replication configuration is the earlier version, V1. In the earlier version, this
     *             element is not allowed</p>
     *          </note>
     */
    ReplicaModifications replicaModifications;

    SourceSelectionCriteria() {
        this.sseKmsEncryptedObjects = null;
        this.replicaModifications = null;
    }

    protected SourceSelectionCriteria(BuilderImpl builder) {
        this.sseKmsEncryptedObjects = builder.sseKmsEncryptedObjects;
        this.replicaModifications = builder.replicaModifications;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(SourceSelectionCriteria.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof SourceSelectionCriteria);
    }

    public SseKmsEncryptedObjects sseKmsEncryptedObjects() {
        return sseKmsEncryptedObjects;
    }

    public ReplicaModifications replicaModifications() {
        return replicaModifications;
    }

    public void setSseKmsEncryptedObjects(final SseKmsEncryptedObjects sseKmsEncryptedObjects) {
        this.sseKmsEncryptedObjects = sseKmsEncryptedObjects;
    }

    public void setReplicaModifications(final ReplicaModifications replicaModifications) {
        this.replicaModifications = replicaModifications;
    }

    public interface Builder {
        Builder sseKmsEncryptedObjects(SseKmsEncryptedObjects sseKmsEncryptedObjects);

        Builder replicaModifications(ReplicaModifications replicaModifications);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p> A container for filter information for the selection of Amazon S3 objects encrypted with AWS
         *          KMS. If you include <code>SourceSelectionCriteria</code> in the replication configuration,
         *          this element is required. </p>
         */
        SseKmsEncryptedObjects sseKmsEncryptedObjects;

        /**
         * <p>A filter that you can specify for selections for modifications on replicas. Amazon S3 doesn't
         *          replicate replica modifications by default. In the latest version of replication
         *          configuration (when <code>Filter</code> is specified), you can specify this element and set
         *          the status to <code>Enabled</code> to replicate modifications on replicas. </p>
         *          <note>
         *             <p> If you don't specify the <code>Filter</code> element, Amazon S3 assumes that the
         *             replication configuration is the earlier version, V1. In the earlier version, this
         *             element is not allowed</p>
         *          </note>
         */
        ReplicaModifications replicaModifications;

        protected BuilderImpl() {
        }

        private BuilderImpl(SourceSelectionCriteria model) {
            sseKmsEncryptedObjects(model.sseKmsEncryptedObjects);
            replicaModifications(model.replicaModifications);
        }

        public SourceSelectionCriteria build() {
            return new SourceSelectionCriteria(this);
        }

        public final Builder sseKmsEncryptedObjects(SseKmsEncryptedObjects sseKmsEncryptedObjects) {
            this.sseKmsEncryptedObjects = sseKmsEncryptedObjects;
            return this;
        }

        public final Builder replicaModifications(ReplicaModifications replicaModifications) {
            this.replicaModifications = replicaModifications;
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

        public SseKmsEncryptedObjects sseKmsEncryptedObjects() {
            return sseKmsEncryptedObjects;
        }

        public ReplicaModifications replicaModifications() {
            return replicaModifications;
        }

        public void setSseKmsEncryptedObjects(final SseKmsEncryptedObjects sseKmsEncryptedObjects) {
            this.sseKmsEncryptedObjects = sseKmsEncryptedObjects;
        }

        public void setReplicaModifications(final ReplicaModifications replicaModifications) {
            this.replicaModifications = replicaModifications;
        }
    }
}
