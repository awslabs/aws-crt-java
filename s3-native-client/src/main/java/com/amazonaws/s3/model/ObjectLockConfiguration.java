// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectLockConfiguration {
    private ObjectLockEnabled objectLockEnabled;

    private ObjectLockRule rule;

    private ObjectLockConfiguration() {
        this.objectLockEnabled = null;
        this.rule = null;
    }

    private ObjectLockConfiguration(Builder builder) {
        this.objectLockEnabled = builder.objectLockEnabled;
        this.rule = builder.rule;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ObjectLockConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ObjectLockConfiguration);
    }

    public ObjectLockEnabled objectLockEnabled() {
        return objectLockEnabled;
    }

    public void setObjectLockEnabled(final ObjectLockEnabled objectLockEnabled) {
        this.objectLockEnabled = objectLockEnabled;
    }

    public ObjectLockRule rule() {
        return rule;
    }

    public void setRule(final ObjectLockRule rule) {
        this.rule = rule;
    }

    static final class Builder {
        private ObjectLockEnabled objectLockEnabled;

        private ObjectLockRule rule;

        private Builder() {
        }

        private Builder(ObjectLockConfiguration model) {
            objectLockEnabled(model.objectLockEnabled);
            rule(model.rule);
        }

        public ObjectLockConfiguration build() {
            return new com.amazonaws.s3.model.ObjectLockConfiguration(this);
        }

        /**
         * <p>Indicates whether this bucket has an Object Lock configuration enabled.</p>
         */
        public final Builder objectLockEnabled(ObjectLockEnabled objectLockEnabled) {
            this.objectLockEnabled = objectLockEnabled;
            return this;
        }

        /**
         * <p>The Object Lock rule in place for the specified object.</p>
         */
        public final Builder rule(ObjectLockRule rule) {
            this.rule = rule;
            return this;
        }
    }
}
