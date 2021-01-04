// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteBucketTaggingResponse {
    DeleteBucketTaggingResponse() {
    }

    protected DeleteBucketTaggingResponse(BuilderImpl builder) {
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(DeleteBucketTaggingResponse.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeleteBucketTaggingResponse);
    }

    public interface Builder {
    }

    protected static class BuilderImpl implements Builder {
        protected BuilderImpl() {
        }

        private BuilderImpl(DeleteBucketTaggingResponse model) {
        }

        public DeleteBucketTaggingResponse build() {
            return new DeleteBucketTaggingResponse(this);
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
