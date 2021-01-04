// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ExistingObjectReplication {
    /**
     * <p></p>
     */
    ExistingObjectReplicationStatus status;

    ExistingObjectReplication() {
        this.status = null;
    }

    protected ExistingObjectReplication(BuilderImpl builder) {
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

    public interface Builder {
        Builder status(ExistingObjectReplicationStatus status);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p></p>
         */
        ExistingObjectReplicationStatus status;

        protected BuilderImpl() {
        }

        private BuilderImpl(ExistingObjectReplication model) {
            status(model.status);
        }

        public ExistingObjectReplication build() {
            return new ExistingObjectReplication(this);
        }

        public final Builder status(ExistingObjectReplicationStatus status) {
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

        public ExistingObjectReplicationStatus status() {
            return status;
        }

        public void setStatus(final ExistingObjectReplicationStatus status) {
            this.status = status;
        }
    }
}
