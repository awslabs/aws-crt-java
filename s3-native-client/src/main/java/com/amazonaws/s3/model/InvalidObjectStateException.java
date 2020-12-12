// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InvalidObjectStateException extends RuntimeException {
    private StorageClass storageClass;

    private IntelligentTieringAccessTier accessTier;

    private InvalidObjectStateException() {
        this.storageClass = null;
        this.accessTier = null;
    }

    private InvalidObjectStateException(Builder builder) {
        this.storageClass = builder.storageClass;
        this.accessTier = builder.accessTier;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(InvalidObjectStateException.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof InvalidObjectStateException);
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public IntelligentTieringAccessTier accessTier() {
        return accessTier;
    }

    public void setAccessTier(final IntelligentTieringAccessTier accessTier) {
        this.accessTier = accessTier;
    }

    static final class Builder {
        private StorageClass storageClass;

        private IntelligentTieringAccessTier accessTier;

        private Builder() {
        }

        private Builder(InvalidObjectStateException model) {
            storageClass(model.storageClass);
            accessTier(model.accessTier);
        }

        public InvalidObjectStateException build() {
            return new com.amazonaws.s3.model.InvalidObjectStateException(this);
        }

        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        public final Builder accessTier(IntelligentTieringAccessTier accessTier) {
            this.accessTier = accessTier;
            return this;
        }
    }
}
