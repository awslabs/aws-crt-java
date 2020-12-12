// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteMarkerEntry {
    private Owner owner;

    private String key;

    private String versionId;

    private Boolean isLatest;

    private Instant lastModified;

    private DeleteMarkerEntry() {
        this.owner = null;
        this.key = null;
        this.versionId = null;
        this.isLatest = null;
        this.lastModified = null;
    }

    private DeleteMarkerEntry(Builder builder) {
        this.owner = builder.owner;
        this.key = builder.key;
        this.versionId = builder.versionId;
        this.isLatest = builder.isLatest;
        this.lastModified = builder.lastModified;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String versionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public Boolean isLatest() {
        return isLatest;
    }

    public void setIsLatest(final Boolean isLatest) {
        this.isLatest = isLatest;
    }

    public Instant lastModified() {
        return lastModified;
    }

    public void setLastModified(final Instant lastModified) {
        this.lastModified = lastModified;
    }

    static final class Builder {
        private Owner owner;

        private String key;

        private String versionId;

        private Boolean isLatest;

        private Instant lastModified;

        private Builder() {
        }

        private Builder(DeleteMarkerEntry model) {
            owner(model.owner);
            key(model.key);
            versionId(model.versionId);
            isLatest(model.isLatest);
            lastModified(model.lastModified);
        }

        public DeleteMarkerEntry build() {
            return new com.amazonaws.s3.model.DeleteMarkerEntry(this);
        }

        /**
         * <p>The account that created the delete marker.></p>
         */
        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }

        /**
         * <p>The object key.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>Version ID of an object.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        /**
         * <p>Specifies whether the object is (true) or is not (false) the latest version of an
         *          object.</p>
         */
        public final Builder isLatest(Boolean isLatest) {
            this.isLatest = isLatest;
            return this;
        }

        /**
         * <p>Date and time the object was last modified.</p>
         */
        public final Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }
    }
}
