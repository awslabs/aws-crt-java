// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteMarkerEntry {
    /**
     * <p>The account that created the delete marker.></p>
     */
    Owner owner;

    /**
     * <p>The object key.</p>
     */
    String key;

    /**
     * <p>Version ID of an object.</p>
     */
    String versionId;

    /**
     * <p>Specifies whether the object is (true) or is not (false) the latest version of an
     *          object.</p>
     */
    Boolean isLatest;

    /**
     * <p>Date and time the object was last modified.</p>
     */
    Instant lastModified;

    DeleteMarkerEntry() {
        this.owner = null;
        this.key = "";
        this.versionId = "";
        this.isLatest = null;
        this.lastModified = null;
    }

    protected DeleteMarkerEntry(BuilderImpl builder) {
        this.owner = builder.owner;
        this.key = builder.key;
        this.versionId = builder.versionId;
        this.isLatest = builder.isLatest;
        this.lastModified = builder.lastModified;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(DeleteMarkerEntry.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeleteMarkerEntry);
    }

    public Owner owner() {
        return owner;
    }

    public String key() {
        return key;
    }

    public String versionId() {
        return versionId;
    }

    public Boolean isLatest() {
        return isLatest;
    }

    public Instant lastModified() {
        return lastModified;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public void setIsLatest(final Boolean isLatest) {
        this.isLatest = isLatest;
    }

    public void setLastModified(final Instant lastModified) {
        this.lastModified = lastModified;
    }

    public interface Builder {
        Builder owner(Owner owner);

        Builder key(String key);

        Builder versionId(String versionId);

        Builder isLatest(Boolean isLatest);

        Builder lastModified(Instant lastModified);

        DeleteMarkerEntry build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The account that created the delete marker.></p>
         */
        Owner owner;

        /**
         * <p>The object key.</p>
         */
        String key;

        /**
         * <p>Version ID of an object.</p>
         */
        String versionId;

        /**
         * <p>Specifies whether the object is (true) or is not (false) the latest version of an
         *          object.</p>
         */
        Boolean isLatest;

        /**
         * <p>Date and time the object was last modified.</p>
         */
        Instant lastModified;

        protected BuilderImpl() {
        }

        private BuilderImpl(DeleteMarkerEntry model) {
            owner(model.owner);
            key(model.key);
            versionId(model.versionId);
            isLatest(model.isLatest);
            lastModified(model.lastModified);
        }

        public DeleteMarkerEntry build() {
            return new DeleteMarkerEntry(this);
        }

        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public final Builder isLatest(Boolean isLatest) {
            this.isLatest = isLatest;
            return this;
        }

        public final Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
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

        public Owner owner() {
            return owner;
        }

        public String key() {
            return key;
        }

        public String versionId() {
            return versionId;
        }

        public Boolean isLatest() {
            return isLatest;
        }

        public Instant lastModified() {
            return lastModified;
        }

        public void setOwner(final Owner owner) {
            this.owner = owner;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setVersionId(final String versionId) {
            this.versionId = versionId;
        }

        public void setIsLatest(final Boolean isLatest) {
            this.isLatest = isLatest;
        }

        public void setLastModified(final Instant lastModified) {
            this.lastModified = lastModified;
        }
    }
}
