// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InventoryFilter {
    /**
     * <p>The prefix that an object must have to be included in the inventory results.</p>
     */
    String prefix;

    InventoryFilter() {
        this.prefix = "";
    }

    protected InventoryFilter(BuilderImpl builder) {
        this.prefix = builder.prefix;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder prefix(String prefix);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The prefix that an object must have to be included in the inventory results.</p>
         */
        String prefix;

        protected BuilderImpl() {
        }

        private BuilderImpl(InventoryFilter model) {
            prefix(model.prefix);
        }

        public InventoryFilter build() {
            return new InventoryFilter(this);
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
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

        public String prefix() {
            return prefix;
        }

        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }
    }
}
