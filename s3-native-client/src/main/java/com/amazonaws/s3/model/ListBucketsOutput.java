// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListBucketsOutput {
    private List<Bucket> buckets;

    private Owner owner;

    private ListBucketsOutput() {
        this.buckets = null;
        this.owner = null;
    }

    private ListBucketsOutput(Builder builder) {
        this.buckets = builder.buckets;
        this.owner = builder.owner;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ListBucketsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListBucketsOutput);
    }

    public List<Bucket> buckets() {
        return buckets;
    }

    public void setBuckets(final List<Bucket> buckets) {
        this.buckets = buckets;
    }

    public Owner owner() {
        return owner;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    static final class Builder {
        private List<Bucket> buckets;

        private Owner owner;

        private Builder() {
        }

        private Builder(ListBucketsOutput model) {
            buckets(model.buckets);
            owner(model.owner);
        }

        public ListBucketsOutput build() {
            return new com.amazonaws.s3.model.ListBucketsOutput(this);
        }

        /**
         * <p>The list of buckets owned by the requestor.</p>
         */
        public final Builder buckets(List<Bucket> buckets) {
            this.buckets = buckets;
            return this;
        }

        /**
         * <p>The owner of the buckets listed.</p>
         */
        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }
    }
}
