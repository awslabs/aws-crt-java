// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CompletedMultipartUpload {
    /**
     * <p>Array of CompletedPart data types.</p>
     */
    List<CompletedPart> parts;

    CompletedMultipartUpload() {
        this.parts = null;
    }

    protected CompletedMultipartUpload(BuilderImpl builder) {
        this.parts = builder.parts;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(CompletedMultipartUpload.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CompletedMultipartUpload);
    }

    public List<CompletedPart> parts() {
        return parts;
    }

    public interface Builder {
        Builder parts(List<CompletedPart> parts);

        CompletedMultipartUpload build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Array of CompletedPart data types.</p>
         */
        List<CompletedPart> parts;

        protected BuilderImpl() {
        }

        private BuilderImpl(CompletedMultipartUpload model) {
            parts(model.parts);
        }

        public CompletedMultipartUpload build() {
            return new CompletedMultipartUpload(this);
        }

        public final Builder parts(List<CompletedPart> parts) {
            this.parts = parts;
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

        public List<CompletedPart> parts() {
            return parts;
        }
    }
}
