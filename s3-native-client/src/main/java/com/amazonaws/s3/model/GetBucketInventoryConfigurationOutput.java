// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketInventoryConfigurationOutput {
    private InventoryConfiguration inventoryConfiguration;

    private GetBucketInventoryConfigurationOutput() {
        this.inventoryConfiguration = null;
    }

    private GetBucketInventoryConfigurationOutput(Builder builder) {
        this.inventoryConfiguration = builder.inventoryConfiguration;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketInventoryConfigurationOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketInventoryConfigurationOutput);
    }

    public InventoryConfiguration inventoryConfiguration() {
        return inventoryConfiguration;
    }

    public void setInventoryConfiguration(final InventoryConfiguration inventoryConfiguration) {
        this.inventoryConfiguration = inventoryConfiguration;
    }

    static final class Builder {
        private InventoryConfiguration inventoryConfiguration;

        private Builder() {
        }

        private Builder(GetBucketInventoryConfigurationOutput model) {
            inventoryConfiguration(model.inventoryConfiguration);
        }

        public GetBucketInventoryConfigurationOutput build() {
            return new com.amazonaws.s3.model.GetBucketInventoryConfigurationOutput(this);
        }

        /**
         * <p>Specifies the inventory configuration.</p>
         */
        public final Builder inventoryConfiguration(InventoryConfiguration inventoryConfiguration) {
            this.inventoryConfiguration = inventoryConfiguration;
            return this;
        }
    }
}
