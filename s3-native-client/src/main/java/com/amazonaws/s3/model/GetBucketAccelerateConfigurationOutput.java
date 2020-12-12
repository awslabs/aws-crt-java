// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketAccelerateConfigurationOutput {
    private BucketAccelerateStatus status;

    private GetBucketAccelerateConfigurationOutput() {
        this.status = null;
    }

    private GetBucketAccelerateConfigurationOutput(Builder builder) {
        this.status = builder.status;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketAccelerateConfigurationOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketAccelerateConfigurationOutput);
    }

    public BucketAccelerateStatus status() {
        return status;
    }

    public void setStatus(final BucketAccelerateStatus status) {
        this.status = status;
    }

    static final class Builder {
        private BucketAccelerateStatus status;

        private Builder() {
        }

        private Builder(GetBucketAccelerateConfigurationOutput model) {
            status(model.status);
        }

        public GetBucketAccelerateConfigurationOutput build() {
            return new com.amazonaws.s3.model.GetBucketAccelerateConfigurationOutput(this);
        }

        /**
         * <p>The accelerate configuration of the bucket.</p>
         */
        public final Builder status(BucketAccelerateStatus status) {
            this.status = status;
            return this;
        }
    }
}
