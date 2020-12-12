// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DefaultRetention {
    private ObjectLockRetentionMode mode;

    private Integer days;

    private Integer years;

    private DefaultRetention() {
        this.mode = null;
        this.days = null;
        this.years = null;
    }

    private DefaultRetention(Builder builder) {
        this.mode = builder.mode;
        this.days = builder.days;
        this.years = builder.years;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(DefaultRetention.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DefaultRetention);
    }

    public ObjectLockRetentionMode mode() {
        return mode;
    }

    public void setMode(final ObjectLockRetentionMode mode) {
        this.mode = mode;
    }

    public Integer days() {
        return days;
    }

    public void setDays(final Integer days) {
        this.days = days;
    }

    public Integer years() {
        return years;
    }

    public void setYears(final Integer years) {
        this.years = years;
    }

    static final class Builder {
        private ObjectLockRetentionMode mode;

        private Integer days;

        private Integer years;

        private Builder() {
        }

        private Builder(DefaultRetention model) {
            mode(model.mode);
            days(model.days);
            years(model.years);
        }

        public DefaultRetention build() {
            return new com.amazonaws.s3.model.DefaultRetention(this);
        }

        /**
         * <p>The default Object Lock retention mode you want to apply to new objects placed in the
         *          specified bucket.</p>
         */
        public final Builder mode(ObjectLockRetentionMode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * <p>The number of days that you want to specify for the default retention period.</p>
         */
        public final Builder days(Integer days) {
            this.days = days;
            return this;
        }

        /**
         * <p>The number of years that you want to specify for the default retention period.</p>
         */
        public final Builder years(Integer years) {
            this.years = years;
            return this;
        }
    }
}
