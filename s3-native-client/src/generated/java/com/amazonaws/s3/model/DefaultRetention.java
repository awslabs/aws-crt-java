// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DefaultRetention {
    /**
     * <p>The default Object Lock retention mode you want to apply to new objects placed in the
     *          specified bucket.</p>
     */
    ObjectLockRetentionMode mode;

    /**
     * <p>The number of days that you want to specify for the default retention period.</p>
     */
    Integer days;

    /**
     * <p>The number of years that you want to specify for the default retention period.</p>
     */
    Integer years;

    DefaultRetention() {
        this.mode = null;
        this.days = null;
        this.years = null;
    }

    protected DefaultRetention(BuilderImpl builder) {
        this.mode = builder.mode;
        this.days = builder.days;
        this.years = builder.years;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public Integer days() {
        return days;
    }

    public Integer years() {
        return years;
    }

    public interface Builder {
        Builder mode(ObjectLockRetentionMode mode);

        Builder days(Integer days);

        Builder years(Integer years);

        DefaultRetention build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The default Object Lock retention mode you want to apply to new objects placed in the
         *          specified bucket.</p>
         */
        ObjectLockRetentionMode mode;

        /**
         * <p>The number of days that you want to specify for the default retention period.</p>
         */
        Integer days;

        /**
         * <p>The number of years that you want to specify for the default retention period.</p>
         */
        Integer years;

        protected BuilderImpl() {
        }

        private BuilderImpl(DefaultRetention model) {
            mode(model.mode);
            days(model.days);
            years(model.years);
        }

        public DefaultRetention build() {
            return new DefaultRetention(this);
        }

        public final Builder mode(ObjectLockRetentionMode mode) {
            this.mode = mode;
            return this;
        }

        public final Builder days(Integer days) {
            this.days = days;
            return this;
        }

        public final Builder years(Integer years) {
            this.years = years;
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

        public ObjectLockRetentionMode mode() {
            return mode;
        }

        public Integer days() {
            return days;
        }

        public Integer years() {
            return years;
        }
    }
}
