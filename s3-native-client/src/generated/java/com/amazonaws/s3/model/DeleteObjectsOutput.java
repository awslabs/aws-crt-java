// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteObjectsOutput {
    /**
     * <p>Container element for a successful delete. It identifies the object that was
     *          successfully deleted.</p>
     */
    List<DeletedObject> deleted;

    RequestCharged requestCharged;

    /**
     * <p>Container for a failed delete operation that describes the object that Amazon S3 attempted to
     *          delete and the error it encountered.</p>
     */
    List<Error> errors;

    DeleteObjectsOutput() {
        this.deleted = null;
        this.requestCharged = null;
        this.errors = null;
    }

    protected DeleteObjectsOutput(BuilderImpl builder) {
        this.deleted = builder.deleted;
        this.requestCharged = builder.requestCharged;
        this.errors = builder.errors;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(DeleteObjectsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeleteObjectsOutput);
    }

    public List<DeletedObject> deleted() {
        return deleted;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public List<Error> errors() {
        return errors;
    }

    public void setDeleted(final List<DeletedObject> deleted) {
        this.deleted = deleted;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    public void setErrors(final List<Error> errors) {
        this.errors = errors;
    }

    public interface Builder {
        Builder deleted(List<DeletedObject> deleted);

        Builder requestCharged(RequestCharged requestCharged);

        Builder errors(List<Error> errors);

        DeleteObjectsOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Container element for a successful delete. It identifies the object that was
         *          successfully deleted.</p>
         */
        List<DeletedObject> deleted;

        RequestCharged requestCharged;

        /**
         * <p>Container for a failed delete operation that describes the object that Amazon S3 attempted to
         *          delete and the error it encountered.</p>
         */
        List<Error> errors;

        protected BuilderImpl() {
        }

        private BuilderImpl(DeleteObjectsOutput model) {
            deleted(model.deleted);
            requestCharged(model.requestCharged);
            errors(model.errors);
        }

        public DeleteObjectsOutput build() {
            return new DeleteObjectsOutput(this);
        }

        public final Builder deleted(List<DeletedObject> deleted) {
            this.deleted = deleted;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }

        public final Builder errors(List<Error> errors) {
            this.errors = errors;
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

        public List<DeletedObject> deleted() {
            return deleted;
        }

        public RequestCharged requestCharged() {
            return requestCharged;
        }

        public List<Error> errors() {
            return errors;
        }

        public void setDeleted(final List<DeletedObject> deleted) {
            this.deleted = deleted;
        }

        public void setRequestCharged(final RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
        }

        public void setErrors(final List<Error> errors) {
            this.errors = errors;
        }
    }
}
