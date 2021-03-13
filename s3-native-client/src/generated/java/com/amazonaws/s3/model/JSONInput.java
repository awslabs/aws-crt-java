// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class JSONInput {
    /**
     * <p>The type of JSON. Valid values: Document, Lines.</p>
     */
    JSONType type;

    JSONInput() {
        this.type = null;
    }

    protected JSONInput(BuilderImpl builder) {
        this.type = builder.type;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(JSONInput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof JSONInput);
    }

    public JSONType type() {
        return type;
    }

    public interface Builder {
        Builder type(JSONType type);

        JSONInput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The type of JSON. Valid values: Document, Lines.</p>
         */
        JSONType type;

        protected BuilderImpl() {
        }

        private BuilderImpl(JSONInput model) {
            type(model.type);
        }

        public JSONInput build() {
            return new JSONInput(this);
        }

        public final Builder type(JSONType type) {
            this.type = type;
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

        public JSONType type() {
            return type;
        }
    }
}
