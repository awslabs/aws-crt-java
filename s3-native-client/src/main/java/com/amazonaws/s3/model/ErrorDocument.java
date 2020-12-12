// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ErrorDocument {
    private String key;

    private ErrorDocument() {
        this.key = null;
    }

    private ErrorDocument(Builder builder) {
        this.key = builder.key;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private String key;

        private Builder() {
        }

        private Builder(ErrorDocument model) {
            key(model.key);
        }

        public ErrorDocument build() {
            return new com.amazonaws.s3.model.ErrorDocument(this);
        }

        /**
         * <p>The object key name to use when a 4XX class error occurs.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }
    }
}
