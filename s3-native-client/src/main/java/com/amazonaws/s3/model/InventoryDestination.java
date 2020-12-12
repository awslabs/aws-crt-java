// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InventoryDestination {
    private InventoryS3BucketDestination s3BucketDestination;

    private InventoryDestination() {
        this.s3BucketDestination = null;
    }

    private InventoryDestination(Builder builder) {
        this.s3BucketDestination = builder.s3BucketDestination;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(InventoryDestination.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof InventoryDestination);
    }

    public InventoryS3BucketDestination s3BucketDestination() {
        return s3BucketDestination;
    }

    public void setS3BucketDestination(final InventoryS3BucketDestination s3BucketDestination) {
        this.s3BucketDestination = s3BucketDestination;
    }

    static final class Builder {
        private InventoryS3BucketDestination s3BucketDestination;

        private Builder() {
        }

        private Builder(InventoryDestination model) {
            s3BucketDestination(model.s3BucketDestination);
        }

        public InventoryDestination build() {
            return new com.amazonaws.s3.model.InventoryDestination(this);
        }

        /**
         * <p>Contains the bucket name, file format, bucket owner (optional), and prefix (optional)
         *          where inventory results are published.</p>
         */
        public final Builder s3BucketDestination(InventoryS3BucketDestination s3BucketDestination) {
            this.s3BucketDestination = s3BucketDestination;
            return this;
        }
    }
}
