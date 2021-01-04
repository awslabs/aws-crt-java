// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AccelerateConfiguration {
    /**
     * <p>Specifies the transfer acceleration status of the bucket.</p>
     */
    BucketAccelerateStatus status;

    AccelerateConfiguration() {
        this.status = null;
    }

    protected AccelerateConfiguration(BuilderImpl builder) {
        this.status = builder.status;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder status(BucketAccelerateStatus status);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the transfer acceleration status of the bucket.</p>
         */
        BucketAccelerateStatus status;

        protected BuilderImpl() {
        }

        private BuilderImpl(AccelerateConfiguration model) {
            status(model.status);
        }

        public AccelerateConfiguration build() {
            return new AccelerateConfiguration(this);
        }

        public final Builder status(BucketAccelerateStatus status) {
            this.status = status;
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

        public BucketAccelerateStatus status() {
            return status;
        }

        public void setStatus(final BucketAccelerateStatus status) {
            this.status = status;
        }
    }
}
