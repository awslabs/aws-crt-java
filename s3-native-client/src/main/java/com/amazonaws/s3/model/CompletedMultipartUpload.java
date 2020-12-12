// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CompletedMultipartUpload {
    private List<CompletedPart> parts;

    private CompletedMultipartUpload() {
        this.parts = null;
    }

    private CompletedMultipartUpload(Builder builder) {
        this.parts = builder.parts;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setParts(final List<CompletedPart> parts) {
        this.parts = parts;
    }

    static final class Builder {
        private List<CompletedPart> parts;

        private Builder() {
        }

        private Builder(CompletedMultipartUpload model) {
            parts(model.parts);
        }

        public CompletedMultipartUpload build() {
            return new com.amazonaws.s3.model.CompletedMultipartUpload(this);
        }

        /**
         * <p>Array of CompletedPart data types.</p>
         */
        public final Builder parts(List<CompletedPart> parts) {
            this.parts = parts;
            return this;
        }
    }
}
