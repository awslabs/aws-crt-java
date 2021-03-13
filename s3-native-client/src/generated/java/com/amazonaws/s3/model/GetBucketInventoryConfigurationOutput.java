// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketInventoryConfigurationOutput {
    /**
     * <p>Specifies the inventory configuration.</p>
     */
    InventoryConfiguration inventoryConfiguration;

    GetBucketInventoryConfigurationOutput() {
        this.inventoryConfiguration = null;
    }

    protected GetBucketInventoryConfigurationOutput(BuilderImpl builder) {
        this.inventoryConfiguration = builder.inventoryConfiguration;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder inventoryConfiguration(InventoryConfiguration inventoryConfiguration);

        GetBucketInventoryConfigurationOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the inventory configuration.</p>
         */
        InventoryConfiguration inventoryConfiguration;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketInventoryConfigurationOutput model) {
            inventoryConfiguration(model.inventoryConfiguration);
        }

        public GetBucketInventoryConfigurationOutput build() {
            return new GetBucketInventoryConfigurationOutput(this);
        }

        public final Builder inventoryConfiguration(InventoryConfiguration inventoryConfiguration) {
            this.inventoryConfiguration = inventoryConfiguration;
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

        public InventoryConfiguration inventoryConfiguration() {
            return inventoryConfiguration;
        }
    }
}
