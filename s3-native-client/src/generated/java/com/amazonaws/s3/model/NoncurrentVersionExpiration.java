// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class NoncurrentVersionExpiration {
    /**
     * <p>Specifies the number of days an object is noncurrent before Amazon S3 can perform the
     *          associated action. For information about the noncurrent days calculations, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/intro-lifecycle-rules.html#non-current-days-calculations">How
     *             Amazon S3 Calculates When an Object Became Noncurrent</a> in the <i>Amazon Simple
     *             Storage Service Developer Guide</i>.</p>
     */
    Integer noncurrentDays;

    NoncurrentVersionExpiration() {
        this.noncurrentDays = null;
    }

    protected NoncurrentVersionExpiration(BuilderImpl builder) {
        this.noncurrentDays = builder.noncurrentDays;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(NoncurrentVersionExpiration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof NoncurrentVersionExpiration);
    }

    public Integer noncurrentDays() {
        return noncurrentDays;
    }

    public void setNoncurrentDays(final Integer noncurrentDays) {
        this.noncurrentDays = noncurrentDays;
    }

    public interface Builder {
        Builder noncurrentDays(Integer noncurrentDays);

        NoncurrentVersionExpiration build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the number of days an object is noncurrent before Amazon S3 can perform the
         *          associated action. For information about the noncurrent days calculations, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/intro-lifecycle-rules.html#non-current-days-calculations">How
         *             Amazon S3 Calculates When an Object Became Noncurrent</a> in the <i>Amazon Simple
         *             Storage Service Developer Guide</i>.</p>
         */
        Integer noncurrentDays;

        protected BuilderImpl() {
        }

        private BuilderImpl(NoncurrentVersionExpiration model) {
            noncurrentDays(model.noncurrentDays);
        }

        public NoncurrentVersionExpiration build() {
            return new NoncurrentVersionExpiration(this);
        }

        public final Builder noncurrentDays(Integer noncurrentDays) {
            this.noncurrentDays = noncurrentDays;
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

        public Integer noncurrentDays() {
            return noncurrentDays;
        }

        public void setNoncurrentDays(final Integer noncurrentDays) {
            this.noncurrentDays = noncurrentDays;
        }
    }
}
