// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Stats {
    private Long bytesScanned;

    private Long bytesProcessed;

    private Long bytesReturned;

    private Stats() {
        this.bytesScanned = null;
        this.bytesProcessed = null;
        this.bytesReturned = null;
    }

    private Stats(Builder builder) {
        this.bytesScanned = builder.bytesScanned;
        this.bytesProcessed = builder.bytesProcessed;
        this.bytesReturned = builder.bytesReturned;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setBytesScanned(final Long bytesScanned) {
        this.bytesScanned = bytesScanned;
    }

    public Long bytesProcessed() {
        return bytesProcessed;
    }

    public void setBytesProcessed(final Long bytesProcessed) {
        this.bytesProcessed = bytesProcessed;
    }

    public Long bytesReturned() {
        return bytesReturned;
    }

    public void setBytesReturned(final Long bytesReturned) {
        this.bytesReturned = bytesReturned;
    }

    static final class Builder {
        private Long bytesScanned;

        private Long bytesProcessed;

        private Long bytesReturned;

        private Builder() {
        }

        private Builder(Stats model) {
            bytesScanned(model.bytesScanned);
            bytesProcessed(model.bytesProcessed);
            bytesReturned(model.bytesReturned);
        }

        public Stats build() {
            return new com.amazonaws.s3.model.Stats(this);
        }

        /**
         * <p>The total number of object bytes scanned.</p>
         */
        public final Builder bytesScanned(Long bytesScanned) {
            this.bytesScanned = bytesScanned;
            return this;
        }

        /**
         * <p>The total number of uncompressed object bytes processed.</p>
         */
        public final Builder bytesProcessed(Long bytesProcessed) {
            this.bytesProcessed = bytesProcessed;
            return this;
        }

        /**
         * <p>The total number of bytes of records payload data returned.</p>
         */
        public final Builder bytesReturned(Long bytesReturned) {
            this.bytesReturned = bytesReturned;
            return this;
        }
    }
}
