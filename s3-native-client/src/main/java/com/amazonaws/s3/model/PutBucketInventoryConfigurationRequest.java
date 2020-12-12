// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketInventoryConfigurationRequest {
    private String bucket;

    private String id;

    private InventoryConfiguration inventoryConfiguration;

    private String expectedBucketOwner;

    private PutBucketInventoryConfigurationRequest() {
        this.bucket = null;
        this.id = null;
        this.inventoryConfiguration = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketInventoryConfigurationRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.id = builder.id;
        this.inventoryConfiguration = builder.inventoryConfiguration;
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
        return Objects.hash(PutBucketInventoryConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketInventoryConfigurationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String id() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public InventoryConfiguration inventoryConfiguration() {
        return inventoryConfiguration;
    }

    public void setInventoryConfiguration(final InventoryConfiguration inventoryConfiguration) {
        this.inventoryConfiguration = inventoryConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String id;

        private InventoryConfiguration inventoryConfiguration;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketInventoryConfigurationRequest model) {
            bucket(model.bucket);
            id(model.id);
            inventoryConfiguration(model.inventoryConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketInventoryConfigurationRequest build() {
            return new com.amazonaws.s3.model.PutBucketInventoryConfigurationRequest(this);
        }

        /**
         * <p>The name of the bucket where the inventory configuration will be stored.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The ID used to identify the inventory configuration.</p>
         */
        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * <p>Specifies the inventory configuration.</p>
         */
        public final Builder inventoryConfiguration(InventoryConfiguration inventoryConfiguration) {
            this.inventoryConfiguration = inventoryConfiguration;
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
