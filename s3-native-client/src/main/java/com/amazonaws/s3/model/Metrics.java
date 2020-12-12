// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Metrics {
    private MetricsStatus status;

    private ReplicationTimeValue eventThreshold;

    private Metrics() {
        this.status = null;
        this.eventThreshold = null;
    }

    private Metrics(Builder builder) {
        this.status = builder.status;
        this.eventThreshold = builder.eventThreshold;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Metrics.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Metrics);
    }

    public MetricsStatus status() {
        return status;
    }

    public void setStatus(final MetricsStatus status) {
        this.status = status;
    }

    public ReplicationTimeValue eventThreshold() {
        return eventThreshold;
    }

    public void setEventThreshold(final ReplicationTimeValue eventThreshold) {
        this.eventThreshold = eventThreshold;
    }

    static final class Builder {
        private MetricsStatus status;

        private ReplicationTimeValue eventThreshold;

        private Builder() {
        }

        private Builder(Metrics model) {
            status(model.status);
            eventThreshold(model.eventThreshold);
        }

        public Metrics build() {
            return new com.amazonaws.s3.model.Metrics(this);
        }

        /**
         * <p> Specifies whether the replication metrics are enabled. </p>
         */
        public final Builder status(MetricsStatus status) {
            this.status = status;
            return this;
        }

        /**
         * <p> A container specifying the time threshold for emitting the
         *             <code>s3:Replication:OperationMissedThreshold</code> event. </p>
         */
        public final Builder eventThreshold(ReplicationTimeValue eventThreshold) {
            this.eventThreshold = eventThreshold;
            return this;
        }
    }
}
