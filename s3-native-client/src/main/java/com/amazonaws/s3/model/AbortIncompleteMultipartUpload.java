// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AbortIncompleteMultipartUpload {
    private Integer daysAfterInitiation;

    private AbortIncompleteMultipartUpload() {
        this.daysAfterInitiation = null;
    }

    private AbortIncompleteMultipartUpload(Builder builder) {
        this.daysAfterInitiation = builder.daysAfterInitiation;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(AbortIncompleteMultipartUpload.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof AbortIncompleteMultipartUpload);
    }

    public Integer daysAfterInitiation() {
        return daysAfterInitiation;
    }

    public void setDaysAfterInitiation(final Integer daysAfterInitiation) {
        this.daysAfterInitiation = daysAfterInitiation;
    }

    static final class Builder {
        private Integer daysAfterInitiation;

        private Builder() {
        }

        private Builder(AbortIncompleteMultipartUpload model) {
            daysAfterInitiation(model.daysAfterInitiation);
        }

        public AbortIncompleteMultipartUpload build() {
            return new com.amazonaws.s3.model.AbortIncompleteMultipartUpload(this);
        }

        /**
         * <p>Specifies the number of days after which Amazon S3 aborts an incomplete multipart
         *          upload.</p>
         */
        public final Builder daysAfterInitiation(Integer daysAfterInitiation) {
            this.daysAfterInitiation = daysAfterInitiation;
            return this;
        }
    }
}
