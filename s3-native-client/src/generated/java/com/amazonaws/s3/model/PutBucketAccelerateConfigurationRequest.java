// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketAccelerateConfigurationRequest {
    /**
     * <p>The name of the bucket for which the accelerate configuration is set.</p>
     */
    String bucket;

    /**
     * <p>Container for setting the transfer acceleration state.</p>
     */
    AccelerateConfiguration accelerateConfiguration;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    PutBucketAccelerateConfigurationRequest() {
        this.bucket = "";
        this.accelerateConfiguration = null;
        this.expectedBucketOwner = "";
    }

    protected PutBucketAccelerateConfigurationRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.accelerateConfiguration = builder.accelerateConfiguration;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(PutBucketAccelerateConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketAccelerateConfigurationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public AccelerateConfiguration accelerateConfiguration() {
        return accelerateConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setAccelerateConfiguration(final AccelerateConfiguration accelerateConfiguration) {
        this.accelerateConfiguration = accelerateConfiguration;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder accelerateConfiguration(AccelerateConfiguration accelerateConfiguration);

        Builder expectedBucketOwner(String expectedBucketOwner);

        PutBucketAccelerateConfigurationRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket for which the accelerate configuration is set.</p>
         */
        String bucket;

        /**
         * <p>Container for setting the transfer acceleration state.</p>
         */
        AccelerateConfiguration accelerateConfiguration;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketAccelerateConfigurationRequest model) {
            bucket(model.bucket);
            accelerateConfiguration(model.accelerateConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketAccelerateConfigurationRequest build() {
            return new PutBucketAccelerateConfigurationRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder accelerateConfiguration(
                AccelerateConfiguration accelerateConfiguration) {
            this.accelerateConfiguration = accelerateConfiguration;
            return this;
        }

        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
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

        public String bucket() {
            return bucket;
        }

        public AccelerateConfiguration accelerateConfiguration() {
            return accelerateConfiguration;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setAccelerateConfiguration(
                final AccelerateConfiguration accelerateConfiguration) {
            this.accelerateConfiguration = accelerateConfiguration;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
