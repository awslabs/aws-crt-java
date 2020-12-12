// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class BucketAlreadyOwnedByYouException extends RuntimeException {
    private BucketAlreadyOwnedByYouException() {
    }

    private BucketAlreadyOwnedByYouException(Builder builder) {
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(BucketAlreadyOwnedByYouException.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof BucketAlreadyOwnedByYouException);
    }

    static final class Builder {
        private Builder() {
        }

        private Builder(BucketAlreadyOwnedByYouException model) {
        }

        public BucketAlreadyOwnedByYouException build() {
            return new com.amazonaws.s3.model.BucketAlreadyOwnedByYouException(this);
        }
    }
}
