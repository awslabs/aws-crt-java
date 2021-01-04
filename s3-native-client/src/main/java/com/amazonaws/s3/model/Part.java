// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Part {
    /**
     * <p>Part number identifying the part. This is a positive integer between 1 and
     *          10,000.</p>
     */
    Integer partNumber;

    /**
     * <p>Date and time at which the part was uploaded.</p>
     */
    Instant lastModified;

    /**
     * <p>Entity tag returned when the part was uploaded.</p>
     */
    String eTag;

    /**
     * <p>Size in bytes of the uploaded part data.</p>
     */
    Integer size;

    Part() {
        this.partNumber = null;
        this.lastModified = null;
        this.eTag = "";
        this.size = null;
    }

    protected Part(BuilderImpl builder) {
        this.partNumber = builder.partNumber;
        this.lastModified = builder.lastModified;
        this.eTag = builder.eTag;
        this.size = builder.size;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public Instant lastModified() {
        return lastModified;
    }

    public String eTag() {
        return eTag;
    }

    public Integer size() {
        return size;
    }

    public void setPartNumber(final Integer partNumber) {
        this.partNumber = partNumber;
    }

    public void setLastModified(final Instant lastModified) {
        this.lastModified = lastModified;
    }

    public void setETag(final String eTag) {
        this.eTag = eTag;
    }

    public void setSize(final Integer size) {
        this.size = size;
    }

    public interface Builder {
        Builder partNumber(Integer partNumber);

        Builder lastModified(Instant lastModified);

        Builder eTag(String eTag);

        Builder size(Integer size);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Part number identifying the part. This is a positive integer between 1 and
         *          10,000.</p>
         */
        Integer partNumber;

        /**
         * <p>Date and time at which the part was uploaded.</p>
         */
        Instant lastModified;

        /**
         * <p>Entity tag returned when the part was uploaded.</p>
         */
        String eTag;

        /**
         * <p>Size in bytes of the uploaded part data.</p>
         */
        Integer size;

        protected BuilderImpl() {
        }

        private BuilderImpl(Part model) {
            partNumber(model.partNumber);
            lastModified(model.lastModified);
            eTag(model.eTag);
            size(model.size);
        }

        public Part build() {
            return new Part(this);
        }

        public final Builder partNumber(Integer partNumber) {
            this.partNumber = partNumber;
            return this;
        }

        public final Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        public final Builder size(Integer size) {
            this.size = size;
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

        public Integer partNumber() {
            return partNumber;
        }

        public Instant lastModified() {
            return lastModified;
        }

        public String eTag() {
            return eTag;
        }

        public Integer size() {
            return size;
        }

        public void setPartNumber(final Integer partNumber) {
            this.partNumber = partNumber;
        }

        public void setLastModified(final Instant lastModified) {
            this.lastModified = lastModified;
        }

        public void setETag(final String eTag) {
            this.eTag = eTag;
        }

        public void setSize(final Integer size) {
            this.size = size;
        }
    }
}
