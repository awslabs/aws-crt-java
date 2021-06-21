// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListBucketsOutput {
    /**
     * <p>The list of buckets owned by the requestor.</p>
     */
    List<Bucket> buckets;

    /**
     * <p>The owner of the buckets listed.</p>
     */
    Owner owner;

    ListBucketsOutput() {
        this.buckets = null;
        this.owner = null;
    }

    protected ListBucketsOutput(BuilderImpl builder) {
        this.buckets = builder.buckets;
        this.owner = builder.owner;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public Owner owner() {
        return owner;
    }

    public interface Builder {
        Builder buckets(List<Bucket> buckets);

        Builder owner(Owner owner);

        ListBucketsOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The list of buckets owned by the requestor.</p>
         */
        List<Bucket> buckets;

        /**
         * <p>The owner of the buckets listed.</p>
         */
        Owner owner;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListBucketsOutput model) {
            buckets(model.buckets);
            owner(model.owner);
        }

        public ListBucketsOutput build() {
            return new ListBucketsOutput(this);
        }

        public final Builder buckets(List<Bucket> buckets) {
            this.buckets = buckets;
            return this;
        }

        public final Builder owner(Owner owner) {
            this.owner = owner;
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

        public List<Bucket> buckets() {
            return buckets;
        }

        public Owner owner() {
            return owner;
        }
    }
}
