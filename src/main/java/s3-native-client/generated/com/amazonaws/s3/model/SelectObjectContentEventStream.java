// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;

class SelectObjectContentEventStream {
    protected SelectObjectContentEventStream(BuilderImpl builder) {
    }

    @Override
    public int hashCode() {
        return Objects.hash(SelectObjectContentEventStream.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof SelectObjectContentEventStream);
    }

    public interface Builder {
        SelectObjectContentEventStream build();
    }

    protected static class BuilderImpl implements Builder {
        protected BuilderImpl() {
        }

        private BuilderImpl(SelectObjectContentEventStream model) {
        }

        public SelectObjectContentEventStream build() {
            return new SelectObjectContentEventStream(this);
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
