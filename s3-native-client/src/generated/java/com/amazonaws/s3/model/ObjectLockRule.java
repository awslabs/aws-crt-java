// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectLockRule {
    /**
     * <p>The default retention period that you want to apply to new objects placed in the
     *          specified bucket.</p>
     */
    DefaultRetention defaultRetention;

    ObjectLockRule() {
        this.defaultRetention = null;
    }

    protected ObjectLockRule(BuilderImpl builder) {
        this.defaultRetention = builder.defaultRetention;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ObjectLockRule.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ObjectLockRule);
    }

    public DefaultRetention defaultRetention() {
        return defaultRetention;
    }

    public void setDefaultRetention(final DefaultRetention defaultRetention) {
        this.defaultRetention = defaultRetention;
    }

    public interface Builder {
        Builder defaultRetention(DefaultRetention defaultRetention);

        ObjectLockRule build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The default retention period that you want to apply to new objects placed in the
         *          specified bucket.</p>
         */
        DefaultRetention defaultRetention;

        protected BuilderImpl() {
        }

        private BuilderImpl(ObjectLockRule model) {
            defaultRetention(model.defaultRetention);
        }

        public ObjectLockRule build() {
            return new ObjectLockRule(this);
        }

        public final Builder defaultRetention(DefaultRetention defaultRetention) {
            this.defaultRetention = defaultRetention;
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

        public DefaultRetention defaultRetention() {
            return defaultRetention;
        }

        public void setDefaultRetention(final DefaultRetention defaultRetention) {
            this.defaultRetention = defaultRetention;
        }
    }
}
