// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InventoryDestination {
    /**
     * <p>Contains the bucket name, file format, bucket owner (optional), and prefix (optional)
     *          where inventory results are published.</p>
     */
    InventoryS3BucketDestination s3BucketDestination;

    InventoryDestination() {
        this.s3BucketDestination = null;
    }

    protected InventoryDestination(BuilderImpl builder) {
        this.s3BucketDestination = builder.s3BucketDestination;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder s3BucketDestination(InventoryS3BucketDestination s3BucketDestination);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Contains the bucket name, file format, bucket owner (optional), and prefix (optional)
         *          where inventory results are published.</p>
         */
        InventoryS3BucketDestination s3BucketDestination;

        protected BuilderImpl() {
        }

        private BuilderImpl(InventoryDestination model) {
            s3BucketDestination(model.s3BucketDestination);
        }

        public InventoryDestination build() {
            return new InventoryDestination(this);
        }

        public final Builder s3BucketDestination(InventoryS3BucketDestination s3BucketDestination) {
            this.s3BucketDestination = s3BucketDestination;
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

        public InventoryS3BucketDestination s3BucketDestination() {
            return s3BucketDestination;
        }

        public void setS3BucketDestination(final InventoryS3BucketDestination s3BucketDestination) {
            this.s3BucketDestination = s3BucketDestination;
        }
    }
}
