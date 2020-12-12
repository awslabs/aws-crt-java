// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class SSES3 {
    private SSES3() {
    }

    private SSES3(Builder builder) {
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SSES3.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof SSES3);
    }

    static final class Builder {
        private Builder() {
        }

        private Builder(SSES3 model) {
        }

        public SSES3 build() {
            return new com.amazonaws.s3.model.SSES3(this);
        }
    }
}
