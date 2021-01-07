// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;

class AnalyticsFilter {
    protected AnalyticsFilter(BuilderImpl builder) {
    }

    @Override
    public int hashCode() {
        return Objects.hash(AnalyticsFilter.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof AnalyticsFilter);
    }

    public interface Builder {
        AnalyticsFilter build();
    }

    protected static class BuilderImpl implements Builder {
        protected BuilderImpl() {
        }

        private BuilderImpl(AnalyticsFilter model) {
        }

        public AnalyticsFilter build() {
            return new AnalyticsFilter(this);
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
