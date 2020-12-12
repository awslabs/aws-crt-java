// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AccelerateConfiguration {
    private BucketAccelerateStatus status;

    private AccelerateConfiguration() {
        this.status = null;
    }

    private AccelerateConfiguration(Builder builder) {
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
        return Objects.hash(AccelerateConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof AccelerateConfiguration);
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

        private Builder(AccelerateConfiguration model) {
            status(model.status);
        }

        public AccelerateConfiguration build() {
            return new com.amazonaws.s3.model.AccelerateConfiguration(this);
        }

        /**
         * <p>Specifies the transfer acceleration status of the bucket.</p>
         */
        public final Builder status(BucketAccelerateStatus status) {
            this.status = status;
            return this;
        }
    }
}
