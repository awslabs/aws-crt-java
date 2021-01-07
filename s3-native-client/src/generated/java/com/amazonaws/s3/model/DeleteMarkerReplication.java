// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteMarkerReplication {
    /**
     * <p>Indicates whether to replicate delete markers.</p>
     *          <note>
     *             <p>Indicates whether to replicate delete markers.</p>
     *          </note>
     */
    DeleteMarkerReplicationStatus status;

    DeleteMarkerReplication() {
        this.status = null;
    }

    protected DeleteMarkerReplication(BuilderImpl builder) {
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

    public interface Builder {
        Builder status(DeleteMarkerReplicationStatus status);

        DeleteMarkerReplication build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Indicates whether to replicate delete markers.</p>
         *          <note>
         *             <p>Indicates whether to replicate delete markers.</p>
         *          </note>
         */
        DeleteMarkerReplicationStatus status;

        protected BuilderImpl() {
        }

        private BuilderImpl(DeleteMarkerReplication model) {
            status(model.status);
        }

        public DeleteMarkerReplication build() {
            return new DeleteMarkerReplication(this);
        }

        public final Builder status(DeleteMarkerReplicationStatus status) {
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

        public DeleteMarkerReplicationStatus status() {
            return status;
        }

        public void setStatus(final DeleteMarkerReplicationStatus status) {
            this.status = status;
        }
    }
}
