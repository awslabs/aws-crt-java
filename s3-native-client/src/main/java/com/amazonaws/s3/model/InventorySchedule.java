// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InventorySchedule {
    private InventoryFrequency frequency;

    private InventorySchedule() {
        this.frequency = null;
    }

    private InventorySchedule(Builder builder) {
        this.frequency = builder.frequency;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setFrequency(final InventoryFrequency frequency) {
        this.frequency = frequency;
    }

    static final class Builder {
        private InventoryFrequency frequency;

        private Builder() {
        }

        private Builder(InventorySchedule model) {
            frequency(model.frequency);
        }

        public InventorySchedule build() {
            return new com.amazonaws.s3.model.InventorySchedule(this);
        }

        /**
         * <p>Specifies how frequently inventory results are produced.</p>
         */
        public final Builder frequency(InventoryFrequency frequency) {
            this.frequency = frequency;
            return this;
        }
    }
}
