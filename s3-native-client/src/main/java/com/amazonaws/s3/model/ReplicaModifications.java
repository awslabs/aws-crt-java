// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ReplicaModifications {
    private ReplicaModificationsStatus status;

    private ReplicaModifications() {
        this.status = null;
    }

    private ReplicaModifications(Builder builder) {
        this.status = builder.status;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private ReplicaModificationsStatus status;

        private Builder() {
        }

        private Builder(ReplicaModifications model) {
            status(model.status);
        }

        public ReplicaModifications build() {
            return new com.amazonaws.s3.model.ReplicaModifications(this);
        }

        /**
         * <p>Specifies whether Amazon S3 replicates modifications on replicas.</p>
         */
        public final Builder status(ReplicaModificationsStatus status) {
            this.status = status;
            return this;
        }
    }
}
