// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ReplicationTimeValue {
    private Integer minutes;

    private ReplicationTimeValue() {
        this.minutes = null;
    }

    private ReplicationTimeValue(Builder builder) {
        this.minutes = builder.minutes;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private Integer minutes;

        private Builder() {
        }

        private Builder(ReplicationTimeValue model) {
            minutes(model.minutes);
        }

        public ReplicationTimeValue build() {
            return new com.amazonaws.s3.model.ReplicationTimeValue(this);
        }

        /**
         * <p> Contains an integer specifying time in minutes. </p>
         *          <p> Valid values: 15 minutes. </p>
         */
        public final Builder minutes(Integer minutes) {
            this.minutes = minutes;
            return this;
        }
    }
}
