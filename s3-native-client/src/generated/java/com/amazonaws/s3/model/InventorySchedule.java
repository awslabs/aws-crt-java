// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InventorySchedule {
    /**
     * <p>Specifies how frequently inventory results are produced.</p>
     */
    InventoryFrequency frequency;

    InventorySchedule() {
        this.frequency = null;
    }

    protected InventorySchedule(BuilderImpl builder) {
        this.frequency = builder.frequency;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(InventorySchedule.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof InventorySchedule);
    }

    public InventoryFrequency frequency() {
        return frequency;
    }

    public interface Builder {
        Builder frequency(InventoryFrequency frequency);

        InventorySchedule build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies how frequently inventory results are produced.</p>
         */
        InventoryFrequency frequency;

        protected BuilderImpl() {
        }

        private BuilderImpl(InventorySchedule model) {
            frequency(model.frequency);
        }

        public InventorySchedule build() {
            return new InventorySchedule(this);
        }

        public final Builder frequency(InventoryFrequency frequency) {
            this.frequency = frequency;
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

        public InventoryFrequency frequency() {
            return frequency;
        }
    }
}
