// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InventoryFilter {
    private String prefix;

    private InventoryFilter() {
        this.prefix = null;
    }

    private InventoryFilter(Builder builder) {
        this.prefix = builder.prefix;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(InventoryFilter.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof InventoryFilter);
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    static final class Builder {
        private String prefix;

        private Builder() {
        }

        private Builder(InventoryFilter model) {
            prefix(model.prefix);
        }

        public InventoryFilter build() {
            return new com.amazonaws.s3.model.InventoryFilter(this);
        }

        /**
         * <p>The prefix that an object must have to be included in the inventory results.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }
    }
}
