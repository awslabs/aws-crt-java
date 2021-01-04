// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class MetadataEntry {
    /**
     * <p>Name of the Object.</p>
     */
    String name;

    /**
     * <p>Value of the Object.</p>
     */
    String value;

    MetadataEntry() {
        this.name = "";
        this.value = "";
    }

    protected MetadataEntry(BuilderImpl builder) {
        this.name = builder.name;
        this.value = builder.value;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String value() {
        return value;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public interface Builder {
        Builder name(String name);

        Builder value(String value);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Name of the Object.</p>
         */
        String name;

        /**
         * <p>Value of the Object.</p>
         */
        String value;

        protected BuilderImpl() {
        }

        private BuilderImpl(MetadataEntry model) {
            name(model.name);
            value(model.value);
        }

        public MetadataEntry build() {
            return new MetadataEntry(this);
        }

        public final Builder name(String name) {
            this.name = name;
            return this;
        }

        public final Builder value(String value) {
            this.value = value;
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

        public String name() {
            return name;
        }

        public String value() {
            return value;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }
}
