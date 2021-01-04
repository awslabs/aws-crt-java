// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteObjectOutput {
    /**
     * <p>Specifies whether the versioned object that was permanently deleted was (true) or was
     *          not (false) a delete marker.</p>
     */
    Boolean deleteMarker;

    /**
     * <p>Returns the version ID of the delete marker created as a result of the DELETE
     *          operation.</p>
     */
    String versionId;

    RequestCharged requestCharged;

    DeleteObjectOutput() {
        this.deleteMarker = null;
        this.versionId = "";
        this.requestCharged = null;
    }

    protected DeleteObjectOutput(BuilderImpl builder) {
        this.deleteMarker = builder.deleteMarker;
        this.versionId = builder.versionId;
        this.requestCharged = builder.requestCharged;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String versionId() {
        return versionId;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public void setDeleteMarker(final Boolean deleteMarker) {
        this.deleteMarker = deleteMarker;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    public interface Builder {
        Builder deleteMarker(Boolean deleteMarker);

        Builder versionId(String versionId);

        Builder requestCharged(RequestCharged requestCharged);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies whether the versioned object that was permanently deleted was (true) or was
         *          not (false) a delete marker.</p>
         */
        Boolean deleteMarker;

        /**
         * <p>Returns the version ID of the delete marker created as a result of the DELETE
         *          operation.</p>
         */
        String versionId;

        RequestCharged requestCharged;

        protected BuilderImpl() {
        }

        private BuilderImpl(DeleteObjectOutput model) {
            deleteMarker(model.deleteMarker);
            versionId(model.versionId);
            requestCharged(model.requestCharged);
        }

        public DeleteObjectOutput build() {
            return new DeleteObjectOutput(this);
        }

        public final Builder deleteMarker(Boolean deleteMarker) {
            this.deleteMarker = deleteMarker;
            return this;
        }

        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
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

        public Boolean deleteMarker() {
            return deleteMarker;
        }

        public String versionId() {
            return versionId;
        }

        public RequestCharged requestCharged() {
            return requestCharged;
        }

        public void setDeleteMarker(final Boolean deleteMarker) {
            this.deleteMarker = deleteMarker;
        }

        public void setVersionId(final String versionId) {
            this.versionId = versionId;
        }

        public void setRequestCharged(final RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
        }
    }
}
