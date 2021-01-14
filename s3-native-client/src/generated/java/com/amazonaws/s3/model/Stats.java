// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Stats {
    /**
     * <p>The total number of object bytes scanned.</p>
     */
    Long bytesScanned;

    /**
     * <p>The total number of uncompressed object bytes processed.</p>
     */
    Long bytesProcessed;

    /**
     * <p>The total number of bytes of records payload data returned.</p>
     */
    Long bytesReturned;

    Stats() {
        this.bytesScanned = null;
        this.bytesProcessed = null;
        this.bytesReturned = null;
    }

    protected Stats(BuilderImpl builder) {
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
        return Objects.hash(Stats.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Stats);
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

    public interface Builder {
        Builder bytesScanned(Long bytesScanned);

        Builder bytesProcessed(Long bytesProcessed);

        Builder bytesReturned(Long bytesReturned);

        Stats build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The total number of object bytes scanned.</p>
         */
        Long bytesScanned;

        /**
         * <p>The total number of uncompressed object bytes processed.</p>
         */
        Long bytesProcessed;

        /**
         * <p>The total number of bytes of records payload data returned.</p>
         */
        Long bytesReturned;

        protected BuilderImpl() {
        }

        private BuilderImpl(Stats model) {
            bytesScanned(model.bytesScanned);
            bytesProcessed(model.bytesProcessed);
            bytesReturned(model.bytesReturned);
        }

        public Stats build() {
            return new Stats(this);
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
    }
}
