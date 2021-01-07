// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ProgressEvent {
    /**
     * <p>The Progress event details.</p>
     */
    Progress details;

    ProgressEvent() {
        this.details = null;
    }

    protected ProgressEvent(BuilderImpl builder) {
        this.details = builder.details;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ProgressEvent.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ProgressEvent);
    }

    public Progress details() {
        return details;
    }

    public void setDetails(final Progress details) {
        this.details = details;
    }

    public interface Builder {
        Builder details(Progress details);

        ProgressEvent build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The Progress event details.</p>
         */
        Progress details;

        protected BuilderImpl() {
        }

        private BuilderImpl(ProgressEvent model) {
            details(model.details);
        }

        public ProgressEvent build() {
            return new ProgressEvent(this);
        }

        public final Builder details(Progress details) {
            this.details = details;
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

        public Progress details() {
            return details;
        }

        public void setDetails(final Progress details) {
            this.details = details;
        }
    }
}
