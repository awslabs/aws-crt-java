// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteObjectOutput {
    private Boolean deleteMarker;

    private String versionId;

    private RequestCharged requestCharged;

    private DeleteObjectOutput() {
        this.deleteMarker = null;
        this.versionId = null;
        this.requestCharged = null;
    }

    private DeleteObjectOutput(Builder builder) {
        this.deleteMarker = builder.deleteMarker;
        this.versionId = builder.versionId;
        this.requestCharged = builder.requestCharged;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(DeleteObjectOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeleteObjectOutput);
    }

    public Boolean deleteMarker() {
        return deleteMarker;
    }

    public void setDeleteMarker(final Boolean deleteMarker) {
        this.deleteMarker = deleteMarker;
    }

    public String versionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    static final class Builder {
        private Boolean deleteMarker;

        private String versionId;

        private RequestCharged requestCharged;

        private Builder() {
        }

        private Builder(DeleteObjectOutput model) {
            deleteMarker(model.deleteMarker);
            versionId(model.versionId);
            requestCharged(model.requestCharged);
        }

        public DeleteObjectOutput build() {
            return new com.amazonaws.s3.model.DeleteObjectOutput(this);
        }

        /**
         * <p>Specifies whether the versioned object that was permanently deleted was (true) or was
         *          not (false) a delete marker.</p>
         */
        public final Builder deleteMarker(Boolean deleteMarker) {
            this.deleteMarker = deleteMarker;
            return this;
        }

        /**
         * <p>Returns the version ID of the delete marker created as a result of the DELETE
         *          operation.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }
    }
}
