// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class EndEvent {
    EndEvent() {
    }

    protected EndEvent(BuilderImpl builder) {
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(EndEvent.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof EndEvent);
    }

    public interface Builder {
        EndEvent build();
    }

    protected static class BuilderImpl implements Builder {
        protected BuilderImpl() {
        }

        private BuilderImpl(EndEvent model) {
        }

        public EndEvent build() {
            return new EndEvent(this);
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
