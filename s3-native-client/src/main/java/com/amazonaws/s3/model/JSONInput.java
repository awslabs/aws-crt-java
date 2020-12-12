// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class JSONInput {
    private JSONType type;

    private JSONInput() {
        this.type = null;
    }

    private JSONInput(Builder builder) {
        this.type = builder.type;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setType(final JSONType type) {
        this.type = type;
    }

    static final class Builder {
        private JSONType type;

        private Builder() {
        }

        private Builder(JSONInput model) {
            type(model.type);
        }

        public JSONInput build() {
            return new com.amazonaws.s3.model.JSONInput(this);
        }

        /**
         * <p>The type of JSON. Valid values: Document, Lines.</p>
         */
        public final Builder type(JSONType type) {
            this.type = type;
            return this;
        }
    }
}
