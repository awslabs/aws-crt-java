// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectRetentionOutput {
    private ObjectLockRetention retention;

    private GetObjectRetentionOutput() {
        this.retention = null;
    }

    private GetObjectRetentionOutput(Builder builder) {
        this.retention = builder.retention;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetObjectRetentionOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectRetentionOutput);
    }

    public ObjectLockRetention retention() {
        return retention;
    }

    public void setRetention(final ObjectLockRetention retention) {
        this.retention = retention;
    }

    static final class Builder {
        private ObjectLockRetention retention;

        private Builder() {
        }

        private Builder(GetObjectRetentionOutput model) {
            retention(model.retention);
        }

        public GetObjectRetentionOutput build() {
            return new com.amazonaws.s3.model.GetObjectRetentionOutput(this);
        }

        /**
         * <p>The container element for an object's retention settings.</p>
         */
        public final Builder retention(ObjectLockRetention retention) {
            this.retention = retention;
            return this;
        }
    }
}
