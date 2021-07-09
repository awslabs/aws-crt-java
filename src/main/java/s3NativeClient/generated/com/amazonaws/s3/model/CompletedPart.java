// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CompletedPart {
    /**
     * <p>Entity tag returned when the part was uploaded.</p>
     */
    String eTag;

    /**
     * <p>Part number that identifies the part. This is a positive integer between 1 and
     *          10,000.</p>
     */
    Integer partNumber;

    CompletedPart() {
        this.eTag = "";
        this.partNumber = null;
    }

    protected CompletedPart(BuilderImpl builder) {
        this.eTag = builder.eTag;
        this.partNumber = builder.partNumber;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(CompletedPart.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CompletedPart);
    }

    public String eTag() {
        return eTag;
    }

    public Integer partNumber() {
        return partNumber;
    }

    public interface Builder {
        Builder eTag(String eTag);

        Builder partNumber(Integer partNumber);

        CompletedPart build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Entity tag returned when the part was uploaded.</p>
         */
        String eTag;

        /**
         * <p>Part number that identifies the part. This is a positive integer between 1 and
         *          10,000.</p>
         */
        Integer partNumber;

        protected BuilderImpl() {
        }

        private BuilderImpl(CompletedPart model) {
            eTag(model.eTag);
            partNumber(model.partNumber);
        }

        public CompletedPart build() {
            return new CompletedPart(this);
        }

        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        public final Builder partNumber(Integer partNumber) {
            this.partNumber = partNumber;
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

        public String eTag() {
            return eTag;
        }

        public Integer partNumber() {
            return partNumber;
        }
    }
}
