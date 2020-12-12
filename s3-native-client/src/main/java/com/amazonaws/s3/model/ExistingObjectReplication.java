// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ExistingObjectReplication {
    private ExistingObjectReplicationStatus status;

    private ExistingObjectReplication() {
        this.status = null;
    }

    private ExistingObjectReplication(Builder builder) {
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
        return Objects.hash(ExistingObjectReplication.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ExistingObjectReplication);
    }

    public ExistingObjectReplicationStatus status() {
        return status;
    }

    public void setStatus(final ExistingObjectReplicationStatus status) {
        this.status = status;
    }

    static final class Builder {
        private ExistingObjectReplicationStatus status;

        private Builder() {
        }

        private Builder(ExistingObjectReplication model) {
            status(model.status);
        }

        public ExistingObjectReplication build() {
            return new com.amazonaws.s3.model.ExistingObjectReplication(this);
        }

        /**
         * <p></p>
         */
        public final Builder status(ExistingObjectReplicationStatus status) {
            this.status = status;
            return this;
        }
    }
}
