// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteMarkerReplication {
    private DeleteMarkerReplicationStatus status;

    private DeleteMarkerReplication() {
        this.status = null;
    }

    private DeleteMarkerReplication(Builder builder) {
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
        return Objects.hash(DeleteMarkerReplication.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeleteMarkerReplication);
    }

    public DeleteMarkerReplicationStatus status() {
        return status;
    }

    public void setStatus(final DeleteMarkerReplicationStatus status) {
        this.status = status;
    }

    static final class Builder {
        private DeleteMarkerReplicationStatus status;

        private Builder() {
        }

        private Builder(DeleteMarkerReplication model) {
            status(model.status);
        }

        public DeleteMarkerReplication build() {
            return new com.amazonaws.s3.model.DeleteMarkerReplication(this);
        }

        /**
         * <p>Indicates whether to replicate delete markers.</p>
         *          <note>
         *             <p>Indicates whether to replicate delete markers.</p>
         *          </note>
         */
        public final Builder status(DeleteMarkerReplicationStatus status) {
            this.status = status;
            return this;
        }
    }
}
