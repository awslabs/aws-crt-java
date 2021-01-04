// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import com.amazonaws.s3.S3Exception;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.ExceptionGenerator")
public class InvalidObjectStateException extends S3Exception {
    protected InvalidObjectStateException(BuilderImpl builder) {
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
        return Objects.hash(InvalidObjectStateException.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof InvalidObjectStateException);
    }

    public interface Builder extends S3Exception.Builder {
    }

    protected static class BuilderImpl extends S3Exception.BuilderImpl implements Builder {
        protected BuilderImpl() {
        }

        private BuilderImpl(InvalidObjectStateException model) {
        }

        public InvalidObjectStateException build() {
            return new InvalidObjectStateException(this);
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
