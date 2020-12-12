// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CopyPartResult {
    private String eTag;

    private Instant lastModified;

    private CopyPartResult() {
        this.eTag = null;
        this.lastModified = null;
    }

    private CopyPartResult(Builder builder) {
        this.eTag = builder.eTag;
        this.lastModified = builder.lastModified;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CopyPartResult.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CopyPartResult);
    }

    public String eTag() {
        return eTag;
    }

    public void setETag(final String eTag) {
        this.eTag = eTag;
    }

    public Instant lastModified() {
        return lastModified;
    }

    public void setLastModified(final Instant lastModified) {
        this.lastModified = lastModified;
    }

    static final class Builder {
        private String eTag;

        private Instant lastModified;

        private Builder() {
        }

        private Builder(CopyPartResult model) {
            eTag(model.eTag);
            lastModified(model.lastModified);
        }

        public CopyPartResult build() {
            return new com.amazonaws.s3.model.CopyPartResult(this);
        }

        /**
         * <p>Entity tag of the object.</p>
         */
        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        /**
         * <p>Date and time at which the object was uploaded.</p>
         */
        public final Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }
    }
}
