// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Bucket {
    private String name;

    private Instant creationDate;

    private Bucket() {
        this.name = null;
        this.creationDate = null;
    }

    private Bucket(Builder builder) {
        this.name = builder.name;
        this.creationDate = builder.creationDate;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setName(final String name) {
        this.name = name;
    }

    public Instant creationDate() {
        return creationDate;
    }

    public void setCreationDate(final Instant creationDate) {
        this.creationDate = creationDate;
    }

    static final class Builder {
        private String name;

        private Instant creationDate;

        private Builder() {
        }

        private Builder(Bucket model) {
            name(model.name);
            creationDate(model.creationDate);
        }

        public Bucket build() {
            return new com.amazonaws.s3.model.Bucket(this);
        }

        /**
         * <p>The name of the bucket.</p>
         */
        public final Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * <p>Date the bucket was created. This date can change when making changes to your bucket, such as editing its bucket policy.</p>
         */
        public final Builder creationDate(Instant creationDate) {
            this.creationDate = creationDate;
            return this;
        }
    }
}
