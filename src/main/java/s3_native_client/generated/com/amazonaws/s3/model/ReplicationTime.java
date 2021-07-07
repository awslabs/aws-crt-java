// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ReplicationTime {
    /**
     * <p> Specifies whether the replication time is enabled. </p>
     */
    ReplicationTimeStatus status;

    /**
     * <p> A container specifying the time by which replication should be complete for all objects
     *          and operations on objects. </p>
     */
    ReplicationTimeValue time;

    ReplicationTime() {
        this.status = null;
        this.time = null;
    }

    protected ReplicationTime(BuilderImpl builder) {
        this.status = builder.status;
        this.time = builder.time;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ReplicationTime.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ReplicationTime);
    }

    public ReplicationTimeStatus status() {
        return status;
    }

    public ReplicationTimeValue time() {
        return time;
    }

    public interface Builder {
        Builder status(ReplicationTimeStatus status);

        Builder time(ReplicationTimeValue time);

        ReplicationTime build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p> Specifies whether the replication time is enabled. </p>
         */
        ReplicationTimeStatus status;

        /**
         * <p> A container specifying the time by which replication should be complete for all objects
         *          and operations on objects. </p>
         */
        ReplicationTimeValue time;

        protected BuilderImpl() {
        }

        private BuilderImpl(ReplicationTime model) {
            status(model.status);
            time(model.time);
        }

        public ReplicationTime build() {
            return new ReplicationTime(this);
        }

        public final Builder status(ReplicationTimeStatus status) {
            this.status = status;
            return this;
        }

        public final Builder time(ReplicationTimeValue time) {
            this.time = time;
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

        public ReplicationTimeStatus status() {
            return status;
        }

        public ReplicationTimeValue time() {
            return time;
        }
    }
}
