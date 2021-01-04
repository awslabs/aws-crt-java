// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class IndexDocument {
    /**
     * <p>A suffix that is appended to a request that is for a directory on the website endpoint
     *          (for example,if the suffix is index.html and you make a request to samplebucket/images/ the
     *          data that is returned will be for the object with the key name images/index.html) The
     *          suffix must not be empty and must not include a slash character.</p>
     */
    String suffix;

    IndexDocument() {
        this.suffix = "";
    }

    protected IndexDocument(BuilderImpl builder) {
        this.suffix = builder.suffix;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder suffix(String suffix);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>A suffix that is appended to a request that is for a directory on the website endpoint
         *          (for example,if the suffix is index.html and you make a request to samplebucket/images/ the
         *          data that is returned will be for the object with the key name images/index.html) The
         *          suffix must not be empty and must not include a slash character.</p>
         */
        String suffix;

        protected BuilderImpl() {
        }

        private BuilderImpl(IndexDocument model) {
            suffix(model.suffix);
        }

        public IndexDocument build() {
            return new IndexDocument(this);
        }

        public final Builder suffix(String suffix) {
            this.suffix = suffix;
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

        public String suffix() {
            return suffix;
        }

        public void setSuffix(final String suffix) {
            this.suffix = suffix;
        }
    }
}
