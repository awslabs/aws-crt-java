// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteObjectsOutput {
    private List<DeletedObject> deleted;

    private RequestCharged requestCharged;

    private List<Error> errors;

    private DeleteObjectsOutput() {
        this.deleted = null;
        this.requestCharged = null;
        this.errors = null;
    }

    private DeleteObjectsOutput(Builder builder) {
        this.deleted = builder.deleted;
        this.requestCharged = builder.requestCharged;
        this.errors = builder.errors;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setDeleted(final List<DeletedObject> deleted) {
        this.deleted = deleted;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    public List<Error> errors() {
        return errors;
    }

    public void setErrors(final List<Error> errors) {
        this.errors = errors;
    }

    static final class Builder {
        private List<DeletedObject> deleted;

        private RequestCharged requestCharged;

        private List<Error> errors;

        private Builder() {
        }

        private Builder(DeleteObjectsOutput model) {
            deleted(model.deleted);
            requestCharged(model.requestCharged);
            errors(model.errors);
        }

        public DeleteObjectsOutput build() {
            return new com.amazonaws.s3.model.DeleteObjectsOutput(this);
        }

        /**
         * <p>Container element for a successful delete. It identifies the object that was
         *          successfully deleted.</p>
         */
        public final Builder deleted(List<DeletedObject> deleted) {
            this.deleted = deleted;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }

        /**
         * <p>Container for a failed delete operation that describes the object that Amazon S3 attempted to
         *          delete and the error it encountered.</p>
         */
        public final Builder errors(List<Error> errors) {
            this.errors = errors;
            return this;
        }
    }
}
