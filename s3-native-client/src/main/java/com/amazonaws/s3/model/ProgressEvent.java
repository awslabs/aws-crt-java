// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ProgressEvent {
    private Progress details;

    private ProgressEvent() {
        this.details = null;
    }

    private ProgressEvent(Builder builder) {
        this.details = builder.details;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private Progress details;

        private Builder() {
        }

        private Builder(ProgressEvent model) {
            details(model.details);
        }

        public ProgressEvent build() {
            return new com.amazonaws.s3.model.ProgressEvent(this);
        }

        /**
         * <p>The Progress event details.</p>
         */
        public final Builder details(Progress details) {
            this.details = details;
            return this;
        }
    }
}
