// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Metrics {
    /**
     * <p> Specifies whether the replication metrics are enabled. </p>
     */
    MetricsStatus status;

    /**
     * <p> A container specifying the time threshold for emitting the
     *             <code>s3:Replication:OperationMissedThreshold</code> event. </p>
     */
    ReplicationTimeValue eventThreshold;

    Metrics() {
        this.status = null;
        this.eventThreshold = null;
    }

    protected Metrics(BuilderImpl builder) {
        this.status = builder.status;
        this.eventThreshold = builder.eventThreshold;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public ReplicationTimeValue eventThreshold() {
        return eventThreshold;
    }

    public void setStatus(final MetricsStatus status) {
        this.status = status;
    }

    public void setEventThreshold(final ReplicationTimeValue eventThreshold) {
        this.eventThreshold = eventThreshold;
    }

    public interface Builder {
        Builder status(MetricsStatus status);

        Builder eventThreshold(ReplicationTimeValue eventThreshold);

        Metrics build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p> Specifies whether the replication metrics are enabled. </p>
         */
        MetricsStatus status;

        /**
         * <p> A container specifying the time threshold for emitting the
         *             <code>s3:Replication:OperationMissedThreshold</code> event. </p>
         */
        ReplicationTimeValue eventThreshold;

        protected BuilderImpl() {
        }

        private BuilderImpl(Metrics model) {
            status(model.status);
            eventThreshold(model.eventThreshold);
        }

        public Metrics build() {
            return new Metrics(this);
        }

        public final Builder status(MetricsStatus status) {
            this.status = status;
            return this;
        }

        public final Builder eventThreshold(ReplicationTimeValue eventThreshold) {
            this.eventThreshold = eventThreshold;
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

        public MetricsStatus status() {
            return status;
        }

        public ReplicationTimeValue eventThreshold() {
            return eventThreshold;
        }

        public void setStatus(final MetricsStatus status) {
            this.status = status;
        }

        public void setEventThreshold(final ReplicationTimeValue eventThreshold) {
            this.eventThreshold = eventThreshold;
        }
    }
}
