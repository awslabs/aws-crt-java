// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ScanRange {
    private Long start;

    private Long end;

    private ScanRange() {
        this.start = null;
        this.end = null;
    }

    private ScanRange(Builder builder) {
        this.start = builder.start;
        this.end = builder.end;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ScanRange.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ScanRange);
    }

    public Long start() {
        return start;
    }

    public void setStart(final Long start) {
        this.start = start;
    }

    public Long end() {
        return end;
    }

    public void setEnd(final Long end) {
        this.end = end;
    }

    static final class Builder {
        private Long start;

        private Long end;

        private Builder() {
        }

        private Builder(ScanRange model) {
            start(model.start);
            end(model.end);
        }

        public ScanRange build() {
            return new com.amazonaws.s3.model.ScanRange(this);
        }

        /**
         * <p>Specifies the start of the byte range. This parameter is optional. Valid values:
         *          non-negative integers. The default value is 0. If only start is supplied, it means scan
         *          from that point to the end of the file.For example;
         *             <code><scanrange><start>50</start></scanrange></code> means scan
         *          from byte 50 until the end of the file.</p>
         */
        public final Builder start(Long start) {
            this.start = start;
            return this;
        }

        /**
         * <p>Specifies the end of the byte range. This parameter is optional. Valid values:
         *          non-negative integers. The default value is one less than the size of the object being
         *          queried. If only the End parameter is supplied, it is interpreted to mean scan the last N
         *          bytes of the file. For example,
         *             <code><scanrange><end>50</end></scanrange></code> means scan the
         *          last 50 bytes.</p>
         */
        public final Builder end(Long end) {
            this.end = end;
            return this;
        }
    }
}
