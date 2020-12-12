// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectAlreadyInActiveTierErrorException extends RuntimeException {
    private ObjectAlreadyInActiveTierErrorException() {
    }

    private ObjectAlreadyInActiveTierErrorException(Builder builder) {
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ObjectAlreadyInActiveTierErrorException.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ObjectAlreadyInActiveTierErrorException);
    }

    static final class Builder {
        private Builder() {
        }

        private Builder(ObjectAlreadyInActiveTierErrorException model) {
        }

        public ObjectAlreadyInActiveTierErrorException build() {
            return new com.amazonaws.s3.model.ObjectAlreadyInActiveTierErrorException(this);
        }
    }
}
