// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketAccelerateConfigurationRequest {
    private String bucket;

    private AccelerateConfiguration accelerateConfiguration;

    private String expectedBucketOwner;

    private PutBucketAccelerateConfigurationRequest() {
        this.bucket = null;
        this.accelerateConfiguration = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketAccelerateConfigurationRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.accelerateConfiguration = builder.accelerateConfiguration;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public AccelerateConfiguration accelerateConfiguration() {
        return accelerateConfiguration;
    }

    public void setAccelerateConfiguration(final AccelerateConfiguration accelerateConfiguration) {
        this.accelerateConfiguration = accelerateConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private AccelerateConfiguration accelerateConfiguration;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketAccelerateConfigurationRequest model) {
            bucket(model.bucket);
            accelerateConfiguration(model.accelerateConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketAccelerateConfigurationRequest build() {
            return new com.amazonaws.s3.model.PutBucketAccelerateConfigurationRequest(this);
        }

        /**
         * <p>The name of the bucket for which the accelerate configuration is set.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Container for setting the transfer acceleration state.</p>
         */
        public final Builder accelerateConfiguration(
                AccelerateConfiguration accelerateConfiguration) {
            this.accelerateConfiguration = accelerateConfiguration;
            return this;
        }

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }
    }
}
