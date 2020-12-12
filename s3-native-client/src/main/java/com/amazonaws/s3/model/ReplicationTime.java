// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ReplicationTime {
    private ReplicationTimeStatus status;

    private ReplicationTimeValue time;

    private ReplicationTime() {
        this.status = null;
        this.time = null;
    }

    private ReplicationTime(Builder builder) {
        this.status = builder.status;
        this.time = builder.time;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setStatus(final ReplicationTimeStatus status) {
        this.status = status;
    }

    public ReplicationTimeValue time() {
        return time;
    }

    public void setTime(final ReplicationTimeValue time) {
        this.time = time;
    }

    static final class Builder {
        private ReplicationTimeStatus status;

        private ReplicationTimeValue time;

        private Builder() {
        }

        private Builder(ReplicationTime model) {
            status(model.status);
            time(model.time);
        }

        public ReplicationTime build() {
            return new com.amazonaws.s3.model.ReplicationTime(this);
        }

        /**
         * <p> Specifies whether the replication time is enabled. </p>
         */
        public final Builder status(ReplicationTimeStatus status) {
            this.status = status;
            return this;
        }

        /**
         * <p> A container specifying the time by which replication should be complete for all objects
         *          and operations on objects. </p>
         */
        public final Builder time(ReplicationTimeValue time) {
            this.time = time;
            return this;
        }
    }
}
