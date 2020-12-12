// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CompletedPart {
    private String eTag;

    private Integer partNumber;

    private CompletedPart() {
        this.eTag = null;
        this.partNumber = null;
    }

    private CompletedPart(Builder builder) {
        this.eTag = builder.eTag;
        this.partNumber = builder.partNumber;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setETag(final String eTag) {
        this.eTag = eTag;
    }

    public Integer partNumber() {
        return partNumber;
    }

    public void setPartNumber(final Integer partNumber) {
        this.partNumber = partNumber;
    }

    static final class Builder {
        private String eTag;

        private Integer partNumber;

        private Builder() {
        }

        private Builder(CompletedPart model) {
            eTag(model.eTag);
            partNumber(model.partNumber);
        }

        public CompletedPart build() {
            return new com.amazonaws.s3.model.CompletedPart(this);
        }

        /**
         * <p>Entity tag returned when the part was uploaded.</p>
         */
        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        /**
         * <p>Part number that identifies the part. This is a positive integer between 1 and
         *          10,000.</p>
         */
        public final Builder partNumber(Integer partNumber) {
            this.partNumber = partNumber;
            return this;
        }
    }
}
