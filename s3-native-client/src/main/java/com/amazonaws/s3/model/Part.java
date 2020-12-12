// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Part {
    private Integer partNumber;

    private Instant lastModified;

    private String eTag;

    private Integer size;

    private Part() {
        this.partNumber = null;
        this.lastModified = null;
        this.eTag = null;
        this.size = null;
    }

    private Part(Builder builder) {
        this.partNumber = builder.partNumber;
        this.lastModified = builder.lastModified;
        this.eTag = builder.eTag;
        this.size = builder.size;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Part.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Part);
    }

    public Integer partNumber() {
        return partNumber;
    }

    public void setPartNumber(final Integer partNumber) {
        this.partNumber = partNumber;
    }

    public Instant lastModified() {
        return lastModified;
    }

    public void setLastModified(final Instant lastModified) {
        this.lastModified = lastModified;
    }

    public String eTag() {
        return eTag;
    }

    public void setETag(final String eTag) {
        this.eTag = eTag;
    }

    public Integer size() {
        return size;
    }

    public void setSize(final Integer size) {
        this.size = size;
    }

    static final class Builder {
        private Integer partNumber;

        private Instant lastModified;

        private String eTag;

        private Integer size;

        private Builder() {
        }

        private Builder(Part model) {
            partNumber(model.partNumber);
            lastModified(model.lastModified);
            eTag(model.eTag);
            size(model.size);
        }

        public Part build() {
            return new com.amazonaws.s3.model.Part(this);
        }

        /**
         * <p>Part number identifying the part. This is a positive integer between 1 and
         *          10,000.</p>
         */
        public final Builder partNumber(Integer partNumber) {
            this.partNumber = partNumber;
            return this;
        }

        /**
         * <p>Date and time at which the part was uploaded.</p>
         */
        public final Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        /**
         * <p>Entity tag returned when the part was uploaded.</p>
         */
        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        /**
         * <p>Size in bytes of the uploaded part data.</p>
         */
        public final Builder size(Integer size) {
            this.size = size;
            return this;
        }
    }
}
