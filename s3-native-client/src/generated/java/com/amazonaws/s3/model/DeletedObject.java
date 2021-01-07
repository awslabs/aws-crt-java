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
public class DeletedObject {
    /**
     * <p>The name of the deleted object.</p>
     */
    String key;

    /**
     * <p>The version ID of the deleted object.</p>
     */
    String versionId;

    /**
     * <p>Specifies whether the versioned object that was permanently deleted was (true) or was
     *          not (false) a delete marker. In a simple DELETE, this header indicates whether (true) or
     *          not (false) a delete marker was created.</p>
     */
    Boolean deleteMarker;

    /**
     * <p>The version ID of the delete marker created as a result of the DELETE operation. If you
     *          delete a specific object version, the value returned by this header is the version ID of
     *          the object version deleted.</p>
     */
    String deleteMarkerVersionId;

    DeletedObject() {
        this.key = "";
        this.versionId = "";
        this.deleteMarker = null;
        this.deleteMarkerVersionId = "";
    }

    protected DeletedObject(BuilderImpl builder) {
        this.key = builder.key;
        this.versionId = builder.versionId;
        this.deleteMarker = builder.deleteMarker;
        this.deleteMarkerVersionId = builder.deleteMarkerVersionId;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(DeletedObject.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeletedObject);
    }

    public String key() {
        return key;
    }

    public String versionId() {
        return versionId;
    }

    public Boolean deleteMarker() {
        return deleteMarker;
    }

    public String deleteMarkerVersionId() {
        return deleteMarkerVersionId;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public void setDeleteMarker(final Boolean deleteMarker) {
        this.deleteMarker = deleteMarker;
    }

    public void setDeleteMarkerVersionId(final String deleteMarkerVersionId) {
        this.deleteMarkerVersionId = deleteMarkerVersionId;
    }

    public interface Builder {
        Builder key(String key);

        Builder versionId(String versionId);

        Builder deleteMarker(Boolean deleteMarker);

        Builder deleteMarkerVersionId(String deleteMarkerVersionId);

        DeletedObject build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the deleted object.</p>
         */
        String key;

        /**
         * <p>The version ID of the deleted object.</p>
         */
        String versionId;

        /**
         * <p>Specifies whether the versioned object that was permanently deleted was (true) or was
         *          not (false) a delete marker. In a simple DELETE, this header indicates whether (true) or
         *          not (false) a delete marker was created.</p>
         */
        Boolean deleteMarker;

        /**
         * <p>The version ID of the delete marker created as a result of the DELETE operation. If you
         *          delete a specific object version, the value returned by this header is the version ID of
         *          the object version deleted.</p>
         */
        String deleteMarkerVersionId;

        protected BuilderImpl() {
        }

        private BuilderImpl(DeletedObject model) {
            key(model.key);
            versionId(model.versionId);
            deleteMarker(model.deleteMarker);
            deleteMarkerVersionId(model.deleteMarkerVersionId);
        }

        public DeletedObject build() {
            return new DeletedObject(this);
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public final Builder deleteMarker(Boolean deleteMarker) {
            this.deleteMarker = deleteMarker;
            return this;
        }

        public final Builder deleteMarkerVersionId(String deleteMarkerVersionId) {
            this.deleteMarkerVersionId = deleteMarkerVersionId;
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

        public String key() {
            return key;
        }

        public String versionId() {
            return versionId;
        }

        public Boolean deleteMarker() {
            return deleteMarker;
        }

        public String deleteMarkerVersionId() {
            return deleteMarkerVersionId;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setVersionId(final String versionId) {
            this.versionId = versionId;
        }

        public void setDeleteMarker(final Boolean deleteMarker) {
            this.deleteMarker = deleteMarker;
        }

        public void setDeleteMarkerVersionId(final String deleteMarkerVersionId) {
            this.deleteMarkerVersionId = deleteMarkerVersionId;
        }
    }
}
