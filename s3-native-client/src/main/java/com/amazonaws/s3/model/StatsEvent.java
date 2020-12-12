// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class StatsEvent {
    private Stats details;

    private StatsEvent() {
        this.details = null;
    }

    private StatsEvent(Builder builder) {
        this.details = builder.details;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private Stats details;

        private Builder() {
        }

        private Builder(StatsEvent model) {
            details(model.details);
        }

        public StatsEvent build() {
            return new com.amazonaws.s3.model.StatsEvent(this);
        }

        /**
         * <p>The Stats event details.</p>
         */
        public final Builder details(Stats details) {
            this.details = details;
            return this;
        }
    }
}
