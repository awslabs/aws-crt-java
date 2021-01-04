// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.Throwable;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.ServiceExceptionGenerator")
public class S3Exception extends RuntimeException {
    String message;

    String requestId;

    int statusCode;

    Throwable cause;

    protected S3Exception(BuilderImpl builder) {
        this.message = builder.message;
        this.requestId = builder.requestId;
        this.statusCode = builder.statusCode;
        this.cause = builder.cause;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(S3Exception.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof S3Exception);
    }

    public String message() {
        return message;
    }

    public String requestId() {
        return requestId;
    }

    public int statusCode() {
        return statusCode;
    }

    public Throwable cause() {
        return cause;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setRequestId(final String requestId) {
        this.requestId = requestId;
    }

    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }

    public void setCause(final Throwable cause) {
        this.cause = cause;
    }

    public interface Builder {
        Builder message(String message);

        Builder requestId(String requestId);

        Builder statusCode(int statusCode);

        Builder cause(Throwable cause);
    }

    protected static class BuilderImpl implements Builder {
        String message;

        String requestId;

        int statusCode;

        Throwable cause;

        protected BuilderImpl() {
        }

        private BuilderImpl(S3Exception model) {
            message(model.message);
            requestId(model.requestId);
            statusCode(model.statusCode);
            cause(model.cause);
        }

        public S3Exception build() {
            return new S3Exception(this);
        }

        public final Builder message(String message) {
            this.message = message;
            return this;
        }

        public final Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public final Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public final Builder cause(Throwable cause) {
            this.cause = cause;
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

        public String message() {
            return message;
        }

        public String requestId() {
            return requestId;
        }

        public int statusCode() {
            return statusCode;
        }

        public Throwable cause() {
            return cause;
        }

        public void setMessage(final String message) {
            this.message = message;
        }

        public void setRequestId(final String requestId) {
            this.requestId = requestId;
        }

        public void setStatusCode(final int statusCode) {
            this.statusCode = statusCode;
        }

        public void setCause(final Throwable cause) {
            this.cause = cause;
        }
    }
}
