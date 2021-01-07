// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ReplicationTimeValue {
    /**
     * <p> Contains an integer specifying time in minutes. </p>
     *          <p> Valid values: 15 minutes. </p>
     */
    Integer minutes;

    ReplicationTimeValue() {
        this.minutes = null;
    }

    protected ReplicationTimeValue(BuilderImpl builder) {
        this.minutes = builder.minutes;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ReplicationTimeValue.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ReplicationTimeValue);
    }

    public Integer minutes() {
        return minutes;
    }

    public void setMinutes(final Integer minutes) {
        this.minutes = minutes;
    }

    public interface Builder {
        Builder minutes(Integer minutes);

        ReplicationTimeValue build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p> Contains an integer specifying time in minutes. </p>
         *          <p> Valid values: 15 minutes. </p>
         */
        Integer minutes;

        protected BuilderImpl() {
        }

        private BuilderImpl(ReplicationTimeValue model) {
            minutes(model.minutes);
        }

        public ReplicationTimeValue build() {
            return new ReplicationTimeValue(this);
        }

        public final Builder minutes(Integer minutes) {
            this.minutes = minutes;
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

        public Integer minutes() {
            return minutes;
        }

        public void setMinutes(final Integer minutes) {
            this.minutes = minutes;
        }
    }
}
