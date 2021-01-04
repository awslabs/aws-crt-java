// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import com.amazonaws.s3.S3Exception;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.ExceptionGenerator")
public class NoSuchUploadException extends S3Exception {
    protected NoSuchUploadException(BuilderImpl builder) {
        super(builder);
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(NoSuchUploadException.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof NoSuchUploadException);
    }

    public interface Builder extends S3Exception.Builder {
    }

    protected static class BuilderImpl extends S3Exception.BuilderImpl implements Builder {
        protected BuilderImpl() {
        }

        private BuilderImpl(NoSuchUploadException model) {
        }

        public NoSuchUploadException build() {
            return new NoSuchUploadException(this);
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
    }
}
