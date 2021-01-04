// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectLockConfiguration {
    /**
     * <p>Indicates whether this bucket has an Object Lock configuration enabled.</p>
     */
    ObjectLockEnabled objectLockEnabled;

    /**
     * <p>The Object Lock rule in place for the specified object.</p>
     */
    ObjectLockRule rule;

    ObjectLockConfiguration() {
        this.objectLockEnabled = null;
        this.rule = null;
    }

    protected ObjectLockConfiguration(BuilderImpl builder) {
        this.objectLockEnabled = builder.objectLockEnabled;
        this.rule = builder.rule;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public ObjectLockRule rule() {
        return rule;
    }

    public void setObjectLockEnabled(final ObjectLockEnabled objectLockEnabled) {
        this.objectLockEnabled = objectLockEnabled;
    }

    public void setRule(final ObjectLockRule rule) {
        this.rule = rule;
    }

    public interface Builder {
        Builder objectLockEnabled(ObjectLockEnabled objectLockEnabled);

        Builder rule(ObjectLockRule rule);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Indicates whether this bucket has an Object Lock configuration enabled.</p>
         */
        ObjectLockEnabled objectLockEnabled;

        /**
         * <p>The Object Lock rule in place for the specified object.</p>
         */
        ObjectLockRule rule;

        protected BuilderImpl() {
        }

        private BuilderImpl(ObjectLockConfiguration model) {
            objectLockEnabled(model.objectLockEnabled);
            rule(model.rule);
        }

        public ObjectLockConfiguration build() {
            return new ObjectLockConfiguration(this);
        }

        public final Builder objectLockEnabled(ObjectLockEnabled objectLockEnabled) {
            this.objectLockEnabled = objectLockEnabled;
            return this;
        }

        public final Builder rule(ObjectLockRule rule) {
            this.rule = rule;
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

        public ObjectLockEnabled objectLockEnabled() {
            return objectLockEnabled;
        }

        public ObjectLockRule rule() {
            return rule;
        }

        public void setObjectLockEnabled(final ObjectLockEnabled objectLockEnabled) {
            this.objectLockEnabled = objectLockEnabled;
        }

        public void setRule(final ObjectLockRule rule) {
            this.rule = rule;
        }
    }
}
