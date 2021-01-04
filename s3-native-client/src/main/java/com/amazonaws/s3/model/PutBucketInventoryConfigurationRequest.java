// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketInventoryConfigurationRequest {
    /**
     * <p>The name of the bucket where the inventory configuration will be stored.</p>
     */
    String bucket;

    /**
     * <p>The ID used to identify the inventory configuration.</p>
     */
    String id;

    /**
     * <p>Specifies the inventory configuration.</p>
     */
    InventoryConfiguration inventoryConfiguration;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    PutBucketInventoryConfigurationRequest() {
        this.bucket = "";
        this.id = "";
        this.inventoryConfiguration = null;
        this.expectedBucketOwner = "";
    }

    protected PutBucketInventoryConfigurationRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.id = builder.id;
        this.inventoryConfiguration = builder.inventoryConfiguration;
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

    public String id() {
        return id;
    }

    public InventoryConfiguration inventoryConfiguration() {
        return inventoryConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setInventoryConfiguration(final InventoryConfiguration inventoryConfiguration) {
        this.inventoryConfiguration = inventoryConfiguration;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder id(String id);

        Builder inventoryConfiguration(InventoryConfiguration inventoryConfiguration);

        Builder expectedBucketOwner(String expectedBucketOwner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket where the inventory configuration will be stored.</p>
         */
        String bucket;

        /**
         * <p>The ID used to identify the inventory configuration.</p>
         */
        String id;

        /**
         * <p>Specifies the inventory configuration.</p>
         */
        InventoryConfiguration inventoryConfiguration;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketInventoryConfigurationRequest model) {
            bucket(model.bucket);
            id(model.id);
            inventoryConfiguration(model.inventoryConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketInventoryConfigurationRequest build() {
            return new PutBucketInventoryConfigurationRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        public final Builder inventoryConfiguration(InventoryConfiguration inventoryConfiguration) {
            this.inventoryConfiguration = inventoryConfiguration;
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

        public String id() {
            return id;
        }

        public InventoryConfiguration inventoryConfiguration() {
            return inventoryConfiguration;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public void setInventoryConfiguration(final InventoryConfiguration inventoryConfiguration) {
            this.inventoryConfiguration = inventoryConfiguration;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
