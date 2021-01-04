// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ReplicaModifications {
    /**
     * <p>Specifies whether Amazon S3 replicates modifications on replicas.</p>
     */
    ReplicaModificationsStatus status;

    ReplicaModifications() {
        this.status = null;
    }

    protected ReplicaModifications(BuilderImpl builder) {
        this.status = builder.status;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ReplicaModifications.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ReplicaModifications);
    }

    public ReplicaModificationsStatus status() {
        return status;
    }

    public void setStatus(final ReplicaModificationsStatus status) {
        this.status = status;
    }

    public interface Builder {
        Builder status(ReplicaModificationsStatus status);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies whether Amazon S3 replicates modifications on replicas.</p>
         */
        ReplicaModificationsStatus status;

        protected BuilderImpl() {
        }

        private BuilderImpl(ReplicaModifications model) {
            status(model.status);
        }

        public ReplicaModifications build() {
            return new ReplicaModifications(this);
        }

        public final Builder status(ReplicaModificationsStatus status) {
            this.status = status;
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

        public ReplicaModificationsStatus status() {
            return status;
        }

        public void setStatus(final ReplicaModificationsStatus status) {
            this.status = status;
        }
    }
}
