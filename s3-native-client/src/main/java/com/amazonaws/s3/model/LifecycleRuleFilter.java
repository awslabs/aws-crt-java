// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;

class LifecycleRuleFilter {
    protected LifecycleRuleFilter(BuilderImpl builder) {
    }

    @Override
    public int hashCode() {
        return Objects.hash(LifecycleRuleFilter.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof LifecycleRuleFilter);
    }

    public interface Builder {
    }

    protected static class BuilderImpl implements Builder {
        protected BuilderImpl() {
        }

        private BuilderImpl(LifecycleRuleFilter model) {
        }

        public LifecycleRuleFilter build() {
            return new LifecycleRuleFilter(this);
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
    }
}
