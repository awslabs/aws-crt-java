// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class NoncurrentVersionTransition {
    private Integer noncurrentDays;

    private TransitionStorageClass storageClass;

    private NoncurrentVersionTransition() {
        this.noncurrentDays = null;
        this.storageClass = null;
    }

    private NoncurrentVersionTransition(Builder builder) {
        this.noncurrentDays = builder.noncurrentDays;
        this.storageClass = builder.storageClass;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setNoncurrentDays(final Integer noncurrentDays) {
        this.noncurrentDays = noncurrentDays;
    }

    public TransitionStorageClass storageClass() {
        return storageClass;
    }

    public void setStorageClass(final TransitionStorageClass storageClass) {
        this.storageClass = storageClass;
    }

    static final class Builder {
        private Integer noncurrentDays;

        private TransitionStorageClass storageClass;

        private Builder() {
        }

        private Builder(NoncurrentVersionTransition model) {
            noncurrentDays(model.noncurrentDays);
            storageClass(model.storageClass);
        }

        public NoncurrentVersionTransition build() {
            return new com.amazonaws.s3.model.NoncurrentVersionTransition(this);
        }

        /**
         * <p>Specifies the number of days an object is noncurrent before Amazon S3 can perform the
         *          associated action. For information about the noncurrent days calculations, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/intro-lifecycle-rules.html#non-current-days-calculations">How
         *             Amazon S3 Calculates How Long an Object Has Been Noncurrent</a> in the
         *             <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder noncurrentDays(Integer noncurrentDays) {
            this.noncurrentDays = noncurrentDays;
            return this;
        }

        /**
         * <p>The class of storage used to store the object.</p>
         */
        public final Builder storageClass(TransitionStorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }
    }
}
