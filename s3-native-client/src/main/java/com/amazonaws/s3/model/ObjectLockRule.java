// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectLockRule {
    private DefaultRetention defaultRetention;

    private ObjectLockRule() {
        this.defaultRetention = null;
    }

    private ObjectLockRule(Builder builder) {
        this.defaultRetention = builder.defaultRetention;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private DefaultRetention defaultRetention;

        private Builder() {
        }

        private Builder(ObjectLockRule model) {
            defaultRetention(model.defaultRetention);
        }

        public ObjectLockRule build() {
            return new com.amazonaws.s3.model.ObjectLockRule(this);
        }

        /**
         * <p>The default retention period that you want to apply to new objects placed in the
         *          specified bucket.</p>
         */
        public final Builder defaultRetention(DefaultRetention defaultRetention) {
            this.defaultRetention = defaultRetention;
            return this;
        }
    }
}
