// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class StatsEvent {
    /**
     * <p>The Stats event details.</p>
     */
    Stats details;

    StatsEvent() {
        this.details = null;
    }

    protected StatsEvent(BuilderImpl builder) {
        this.details = builder.details;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(StatsEvent.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof StatsEvent);
    }

    public Stats details() {
        return details;
    }

    public void setDetails(final Stats details) {
        this.details = details;
    }

    public interface Builder {
        Builder details(Stats details);

        StatsEvent build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The Stats event details.</p>
         */
        Stats details;

        protected BuilderImpl() {
        }

        private BuilderImpl(StatsEvent model) {
            details(model.details);
        }

        public StatsEvent build() {
            return new StatsEvent(this);
        }

        public final Builder details(Stats details) {
            this.details = details;
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

        public Stats details() {
            return details;
        }

        public void setDetails(final Stats details) {
            this.details = details;
        }
    }
}
