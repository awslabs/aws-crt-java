// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class OutputLocation {
    private S3Location s3;

    private OutputLocation() {
        this.s3 = null;
    }

    private OutputLocation(Builder builder) {
        this.s3 = builder.s3;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(OutputLocation.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof OutputLocation);
    }

    public S3Location s3() {
        return s3;
    }

    public void setS3(final S3Location s3) {
        this.s3 = s3;
    }

    static final class Builder {
        private S3Location s3;

        private Builder() {
        }

        private Builder(OutputLocation model) {
            s3(model.s3);
        }

        public OutputLocation build() {
            return new com.amazonaws.s3.model.OutputLocation(this);
        }

        /**
         * <p>Describes an S3 location that will receive the results of the restore request.</p>
         */
        public final Builder s3(S3Location s3) {
            this.s3 = s3;
            return this;
        }
    }
}
