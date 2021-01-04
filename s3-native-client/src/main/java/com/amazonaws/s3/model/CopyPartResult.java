// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CopyPartResult {
    /**
     * <p>Entity tag of the object.</p>
     */
    String eTag;

    /**
     * <p>Date and time at which the object was uploaded.</p>
     */
    Instant lastModified;

    CopyPartResult() {
        this.eTag = "";
        this.lastModified = null;
    }

    protected CopyPartResult(BuilderImpl builder) {
        this.eTag = builder.eTag;
        this.lastModified = builder.lastModified;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public Instant lastModified() {
        return lastModified;
    }

    public void setETag(final String eTag) {
        this.eTag = eTag;
    }

    public void setLastModified(final Instant lastModified) {
        this.lastModified = lastModified;
    }

    public interface Builder {
        Builder eTag(String eTag);

        Builder lastModified(Instant lastModified);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Entity tag of the object.</p>
         */
        String eTag;

        /**
         * <p>Date and time at which the object was uploaded.</p>
         */
        Instant lastModified;

        protected BuilderImpl() {
        }

        private BuilderImpl(CopyPartResult model) {
            eTag(model.eTag);
            lastModified(model.lastModified);
        }

        public CopyPartResult build() {
            return new CopyPartResult(this);
        }

        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        public final Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
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

        public String eTag() {
            return eTag;
        }

        public Instant lastModified() {
            return lastModified;
        }

        public void setETag(final String eTag) {
            this.eTag = eTag;
        }

        public void setLastModified(final Instant lastModified) {
            this.lastModified = lastModified;
        }
    }
}
