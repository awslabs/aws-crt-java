// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class NoncurrentVersionTransition {
    /**
     * <p>Specifies the number of days an object is noncurrent before Amazon S3 can perform the
     *          associated action. For information about the noncurrent days calculations, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/intro-lifecycle-rules.html#non-current-days-calculations">How
     *             Amazon S3 Calculates How Long an Object Has Been Noncurrent</a> in the
     *             <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    Integer noncurrentDays;

    /**
     * <p>The class of storage used to store the object.</p>
     */
    TransitionStorageClass storageClass;

    NoncurrentVersionTransition() {
        this.noncurrentDays = null;
        this.storageClass = null;
    }

    protected NoncurrentVersionTransition(BuilderImpl builder) {
        this.noncurrentDays = builder.noncurrentDays;
        this.storageClass = builder.storageClass;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(NoncurrentVersionTransition.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof NoncurrentVersionTransition);
    }

    public Integer noncurrentDays() {
        return noncurrentDays;
    }

    public TransitionStorageClass storageClass() {
        return storageClass;
    }

    public interface Builder {
        Builder noncurrentDays(Integer noncurrentDays);

        Builder storageClass(TransitionStorageClass storageClass);

        NoncurrentVersionTransition build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the number of days an object is noncurrent before Amazon S3 can perform the
         *          associated action. For information about the noncurrent days calculations, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/intro-lifecycle-rules.html#non-current-days-calculations">How
         *             Amazon S3 Calculates How Long an Object Has Been Noncurrent</a> in the
         *             <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        Integer noncurrentDays;

        /**
         * <p>The class of storage used to store the object.</p>
         */
        TransitionStorageClass storageClass;

        protected BuilderImpl() {
        }

        private BuilderImpl(NoncurrentVersionTransition model) {
            noncurrentDays(model.noncurrentDays);
            storageClass(model.storageClass);
        }

        public NoncurrentVersionTransition build() {
            return new NoncurrentVersionTransition(this);
        }

        public final Builder noncurrentDays(Integer noncurrentDays) {
            this.noncurrentDays = noncurrentDays;
            return this;
        }

        public final Builder storageClass(TransitionStorageClass storageClass) {
            this.storageClass = storageClass;
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

        public TransitionStorageClass storageClass() {
            return storageClass;
        }
    }
}
