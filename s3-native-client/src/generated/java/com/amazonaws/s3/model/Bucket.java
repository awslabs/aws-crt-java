// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Bucket {
    /**
     * <p>The name of the bucket.</p>
     */
    String name;

    /**
     * <p>Date the bucket was created. This date can change when making changes to your bucket, such as editing its bucket policy.</p>
     */
    Instant creationDate;

    Bucket() {
        this.name = "";
        this.creationDate = null;
    }

    protected Bucket(BuilderImpl builder) {
        this.name = builder.name;
        this.creationDate = builder.creationDate;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(Bucket.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Bucket);
    }

    public String name() {
        return name;
    }

    public Instant creationDate() {
        return creationDate;
    }

    public interface Builder {
        Builder name(String name);

        Builder creationDate(Instant creationDate);

        Bucket build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket.</p>
         */
        String name;

        /**
         * <p>Date the bucket was created. This date can change when making changes to your bucket, such as editing its bucket policy.</p>
         */
        Instant creationDate;

        protected BuilderImpl() {
        }

        private BuilderImpl(Bucket model) {
            name(model.name);
            creationDate(model.creationDate);
        }

        public Bucket build() {
            return new Bucket(this);
        }

        public final Builder name(String name) {
            this.name = name;
            return this;
        }

        public final Builder creationDate(Instant creationDate) {
            this.creationDate = creationDate;
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

        public String name() {
            return name;
        }

        public Instant creationDate() {
            return creationDate;
        }
    }
}
