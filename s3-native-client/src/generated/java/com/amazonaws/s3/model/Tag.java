// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Tag {
    /**
     * <p>Name of the object key.</p>
     */
    String key;

    /**
     * <p>Value of the tag.</p>
     */
    String value;

    Tag() {
        this.key = "";
        this.value = "";
    }

    protected Tag(BuilderImpl builder) {
        this.key = builder.key;
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

    public String value() {
        return value;
    }

    public interface Builder {
        Builder key(String key);

        Builder value(String value);

        Tag build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Name of the object key.</p>
         */
        String key;

        /**
         * <p>Value of the tag.</p>
         */
        String value;

        protected BuilderImpl() {
        }

        private BuilderImpl(Tag model) {
            key(model.key);
            value(model.value);
        }

        public Tag build() {
            return new Tag(this);
        }

        public final Builder key(String key) {
            this.key = key;
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

        public String key() {
            return key;
        }

        public String value() {
            return value;
        }
    }
}
