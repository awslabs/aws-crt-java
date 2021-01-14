// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ScanRange {
    /**
     * <p>Specifies the start of the byte range. This parameter is optional. Valid values:
     *          non-negative integers. The default value is 0. If only start is supplied, it means scan
     *          from that point to the end of the file.For example;
     *             <code><scanrange><start>50</start></scanrange></code> means scan
     *          from byte 50 until the end of the file.</p>
     */
    Long start;

    /**
     * <p>Specifies the end of the byte range. This parameter is optional. Valid values:
     *          non-negative integers. The default value is one less than the size of the object being
     *          queried. If only the End parameter is supplied, it is interpreted to mean scan the last N
     *          bytes of the file. For example,
     *             <code><scanrange><end>50</end></scanrange></code> means scan the
     *          last 50 bytes.</p>
     */
    Long end;

    ScanRange() {
        this.start = null;
        this.end = null;
    }

    protected ScanRange(BuilderImpl builder) {
        this.start = builder.start;
        this.end = builder.end;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public Long end() {
        return end;
    }

    public interface Builder {
        Builder start(Long start);

        Builder end(Long end);

        ScanRange build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the start of the byte range. This parameter is optional. Valid values:
         *          non-negative integers. The default value is 0. If only start is supplied, it means scan
         *          from that point to the end of the file.For example;
         *             <code><scanrange><start>50</start></scanrange></code> means scan
         *          from byte 50 until the end of the file.</p>
         */
        Long start;

        /**
         * <p>Specifies the end of the byte range. This parameter is optional. Valid values:
         *          non-negative integers. The default value is one less than the size of the object being
         *          queried. If only the End parameter is supplied, it is interpreted to mean scan the last N
         *          bytes of the file. For example,
         *             <code><scanrange><end>50</end></scanrange></code> means scan the
         *          last 50 bytes.</p>
         */
        Long end;

        protected BuilderImpl() {
        }

        private BuilderImpl(ScanRange model) {
            start(model.start);
            end(model.end);
        }

        public ScanRange build() {
            return new ScanRange(this);
        }

        public final Builder start(Long start) {
            this.start = start;
            return this;
        }

        public final Builder end(Long end) {
            this.end = end;
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

        public Long start() {
            return start;
        }

        public Long end() {
            return end;
        }
    }
}
