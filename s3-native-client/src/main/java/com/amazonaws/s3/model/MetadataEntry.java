// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class MetadataEntry {
    private String name;

    private String value;

    private MetadataEntry() {
        this.name = null;
        this.value = null;
    }

    private MetadataEntry(Builder builder) {
        this.name = builder.name;
        this.value = builder.value;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(MetadataEntry.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof MetadataEntry);
    }

    public String name() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String value() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    static final class Builder {
        private String name;

        private String value;

        private Builder() {
        }

        private Builder(MetadataEntry model) {
            name(model.name);
            value(model.value);
        }

        public MetadataEntry build() {
            return new com.amazonaws.s3.model.MetadataEntry(this);
        }

        /**
         * <p>Name of the Object.</p>
         */
        public final Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * <p>Value of the Object.</p>
         */
        public final Builder value(String value) {
            this.value = value;
            return this;
        }
    }
}
