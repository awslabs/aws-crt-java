// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectVersion {
    private String eTag;

    private Integer size;

    private ObjectVersionStorageClass storageClass;

    private String key;

    private String versionId;

    private Boolean isLatest;

    private Instant lastModified;

    private Owner owner;

    private ObjectVersion() {
        this.eTag = null;
        this.size = null;
        this.storageClass = null;
        this.key = null;
        this.versionId = null;
        this.isLatest = null;
        this.lastModified = null;
        this.owner = null;
    }

    private ObjectVersion(Builder builder) {
        this.eTag = builder.eTag;
        this.size = builder.size;
        this.storageClass = builder.storageClass;
        this.key = builder.key;
        this.versionId = builder.versionId;
        this.isLatest = builder.isLatest;
        this.lastModified = builder.lastModified;
        this.owner = builder.owner;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ObjectVersion.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ObjectVersion);
    }

    public String eTag() {
        return eTag;
    }

    public void setETag(final String eTag) {
        this.eTag = eTag;
    }

    public Integer size() {
        return size;
    }

    public void setSize(final Integer size) {
        this.size = size;
    }

    public ObjectVersionStorageClass storageClass() {
        return storageClass;
    }

    public void setStorageClass(final ObjectVersionStorageClass storageClass) {
        this.storageClass = storageClass;
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

    public Owner owner() {
        return owner;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    static final class Builder {
        private String eTag;

        private Integer size;

        private ObjectVersionStorageClass storageClass;

        private String key;

        private String versionId;

        private Boolean isLatest;

        private Instant lastModified;

        private Owner owner;

        private Builder() {
        }

        private Builder(ObjectVersion model) {
            eTag(model.eTag);
            size(model.size);
            storageClass(model.storageClass);
            key(model.key);
            versionId(model.versionId);
            isLatest(model.isLatest);
            lastModified(model.lastModified);
            owner(model.owner);
        }

        public ObjectVersion build() {
            return new com.amazonaws.s3.model.ObjectVersion(this);
        }

        /**
         * <p>The entity tag is an MD5 hash of that version of the object.</p>
         */
        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        /**
         * <p>Size in bytes of the object.</p>
         */
        public final Builder size(Integer size) {
            this.size = size;
            return this;
        }

        /**
         * <p>The class of storage used to store the object.</p>
         */
        public final Builder storageClass(ObjectVersionStorageClass storageClass) {
            this.storageClass = storageClass;
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

        /**
         * <p>Specifies the owner of the object.</p>
         */
        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }
    }
}
