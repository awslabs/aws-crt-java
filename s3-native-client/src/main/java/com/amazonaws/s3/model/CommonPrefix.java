// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CommonPrefix {
    private String prefix;

    private CommonPrefix() {
        this.prefix = null;
    }

    private CommonPrefix(Builder builder) {
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
        return Objects.hash(CommonPrefix.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CommonPrefix);
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

        private Builder(CommonPrefix model) {
            prefix(model.prefix);
        }

        public CommonPrefix build() {
            return new com.amazonaws.s3.model.CommonPrefix(this);
        }

        /**
         * <p>Container for the specified common prefix.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }
    }
}
