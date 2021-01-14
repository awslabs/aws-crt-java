// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AbortIncompleteMultipartUpload {
    /**
     * <p>Specifies the number of days after which Amazon S3 aborts an incomplete multipart
     *          upload.</p>
     */
    Integer daysAfterInitiation;

    AbortIncompleteMultipartUpload() {
        this.daysAfterInitiation = null;
    }

    protected AbortIncompleteMultipartUpload(BuilderImpl builder) {
        this.daysAfterInitiation = builder.daysAfterInitiation;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder daysAfterInitiation(Integer daysAfterInitiation);

        AbortIncompleteMultipartUpload build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the number of days after which Amazon S3 aborts an incomplete multipart
         *          upload.</p>
         */
        Integer daysAfterInitiation;

        protected BuilderImpl() {
        }

        private BuilderImpl(AbortIncompleteMultipartUpload model) {
            daysAfterInitiation(model.daysAfterInitiation);
        }

        public AbortIncompleteMultipartUpload build() {
            return new AbortIncompleteMultipartUpload(this);
        }

        public final Builder daysAfterInitiation(Integer daysAfterInitiation) {
            this.daysAfterInitiation = daysAfterInitiation;
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

        public Integer daysAfterInitiation() {
            return daysAfterInitiation;
        }
    }
}
