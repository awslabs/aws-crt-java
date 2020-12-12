// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Tag {
    private String key;

    private String value;

    private Tag() {
        this.key = null;
        this.value = null;
    }

    private Tag(Builder builder) {
        this.key = builder.key;
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
        return Objects.hash(Tag.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Tag);
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String value() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    static final class Builder {
        private String key;

        private String value;

        private Builder() {
        }

        private Builder(Tag model) {
            key(model.key);
            value(model.value);
        }

        public Tag build() {
            return new com.amazonaws.s3.model.Tag(this);
        }

        /**
         * <p>Name of the object key.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>Value of the tag.</p>
         */
        public final Builder value(String value) {
            this.value = value;
            return this;
        }
    }
}
