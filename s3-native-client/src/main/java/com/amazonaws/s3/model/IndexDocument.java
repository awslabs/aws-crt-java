// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class IndexDocument {
    private String suffix;

    private IndexDocument() {
        this.suffix = null;
    }

    private IndexDocument(Builder builder) {
        this.suffix = builder.suffix;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IndexDocument.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof IndexDocument);
    }

    public String suffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    static final class Builder {
        private String suffix;

        private Builder() {
        }

        private Builder(IndexDocument model) {
            suffix(model.suffix);
        }

        public IndexDocument build() {
            return new com.amazonaws.s3.model.IndexDocument(this);
        }

        /**
         * <p>A suffix that is appended to a request that is for a directory on the website endpoint
         *          (for example,if the suffix is index.html and you make a request to samplebucket/images/ the
         *          data that is returned will be for the object with the key name images/index.html) The
         *          suffix must not be empty and must not include a slash character.</p>
         */
        public final Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }
    }
}
