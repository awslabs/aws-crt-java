// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteBucketEncryptionResponse {
    DeleteBucketEncryptionResponse() {
    }

    protected DeleteBucketEncryptionResponse(BuilderImpl builder) {
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(DeleteBucketEncryptionResponse.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeleteBucketEncryptionResponse);
    }

    public interface Builder {
    }

    protected static class BuilderImpl implements Builder {
        protected BuilderImpl() {
        }

        private BuilderImpl(DeleteBucketEncryptionResponse model) {
        }

        public DeleteBucketEncryptionResponse build() {
            return new DeleteBucketEncryptionResponse(this);
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
