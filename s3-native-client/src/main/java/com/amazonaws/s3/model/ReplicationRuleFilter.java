// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;

class ReplicationRuleFilter {
    protected ReplicationRuleFilter(BuilderImpl builder) {
    }

    @Override
    public int hashCode() {
        return Objects.hash(ReplicationRuleFilter.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ReplicationRuleFilter);
    }

    public interface Builder {
    }

    protected static class BuilderImpl implements Builder {
        protected BuilderImpl() {
        }

        private BuilderImpl(ReplicationRuleFilter model) {
        }

        public ReplicationRuleFilter build() {
            return new ReplicationRuleFilter(this);
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
