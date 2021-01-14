// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class OutputLocation {
    /**
     * <p>Describes an S3 location that will receive the results of the restore request.</p>
     */
    S3Location s3;

    OutputLocation() {
        this.s3 = null;
    }

    protected OutputLocation(BuilderImpl builder) {
        this.s3 = builder.s3;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder s3(S3Location s3);

        OutputLocation build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Describes an S3 location that will receive the results of the restore request.</p>
         */
        S3Location s3;

        protected BuilderImpl() {
        }

        private BuilderImpl(OutputLocation model) {
            s3(model.s3);
        }

        public OutputLocation build() {
            return new OutputLocation(this);
        }

        public final Builder s3(S3Location s3) {
            this.s3 = s3;
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

        public S3Location s3() {
            return s3;
        }
    }
}
