// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Progress {
    /**
     * <p>The current number of object bytes scanned.</p>
     */
    Long bytesScanned;

    /**
     * <p>The current number of uncompressed object bytes processed.</p>
     */
    Long bytesProcessed;

    /**
     * <p>The current number of bytes of records payload data returned.</p>
     */
    Long bytesReturned;

    Progress() {
        this.bytesScanned = null;
        this.bytesProcessed = null;
        this.bytesReturned = null;
    }

    protected Progress(BuilderImpl builder) {
        this.bytesScanned = builder.bytesScanned;
        this.bytesProcessed = builder.bytesProcessed;
        this.bytesReturned = builder.bytesReturned;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(Progress.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Progress);
    }

    public Long bytesScanned() {
        return bytesScanned;
    }

    public Long bytesProcessed() {
        return bytesProcessed;
    }

    public Long bytesReturned() {
        return bytesReturned;
    }

    public void setBytesScanned(final Long bytesScanned) {
        this.bytesScanned = bytesScanned;
    }

    public void setBytesProcessed(final Long bytesProcessed) {
        this.bytesProcessed = bytesProcessed;
    }

    public void setBytesReturned(final Long bytesReturned) {
        this.bytesReturned = bytesReturned;
    }

    public interface Builder {
        Builder bytesScanned(Long bytesScanned);

        Builder bytesProcessed(Long bytesProcessed);

        Builder bytesReturned(Long bytesReturned);

        Progress build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The current number of object bytes scanned.</p>
         */
        Long bytesScanned;

        /**
         * <p>The current number of uncompressed object bytes processed.</p>
         */
        Long bytesProcessed;

        /**
         * <p>The current number of bytes of records payload data returned.</p>
         */
        Long bytesReturned;

        protected BuilderImpl() {
        }

        private BuilderImpl(Progress model) {
            bytesScanned(model.bytesScanned);
            bytesProcessed(model.bytesProcessed);
            bytesReturned(model.bytesReturned);
        }

        public Progress build() {
            return new Progress(this);
        }

        public final Builder bytesScanned(Long bytesScanned) {
            this.bytesScanned = bytesScanned;
            return this;
        }

        public final Builder bytesProcessed(Long bytesProcessed) {
            this.bytesProcessed = bytesProcessed;
            return this;
        }

        public final Builder bytesReturned(Long bytesReturned) {
            this.bytesReturned = bytesReturned;
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

        public Long bytesScanned() {
            return bytesScanned;
        }

        public Long bytesProcessed() {
            return bytesProcessed;
        }

        public Long bytesReturned() {
            return bytesReturned;
        }

        public void setBytesScanned(final Long bytesScanned) {
            this.bytesScanned = bytesScanned;
        }

        public void setBytesProcessed(final Long bytesProcessed) {
            this.bytesProcessed = bytesProcessed;
        }

        public void setBytesReturned(final Long bytesReturned) {
            this.bytesReturned = bytesReturned;
        }
    }
}
