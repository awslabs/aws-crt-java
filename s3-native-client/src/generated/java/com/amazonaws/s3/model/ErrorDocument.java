// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ErrorDocument {
    /**
     * <p>The object key name to use when a 4XX class error occurs.</p>
     */
    String key;

    ErrorDocument() {
        this.key = "";
    }

    protected ErrorDocument(BuilderImpl builder) {
        this.key = builder.key;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ErrorDocument.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ErrorDocument);
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public interface Builder {
        Builder key(String key);

        ErrorDocument build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The object key name to use when a 4XX class error occurs.</p>
         */
        String key;

        protected BuilderImpl() {
        }

        private BuilderImpl(ErrorDocument model) {
            key(model.key);
        }

        public ErrorDocument build() {
            return new ErrorDocument(this);
        }

        public final Builder key(String key) {
            this.key = key;
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

        public String key() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }
    }
}
