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
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectVersion {
    /**
     * <p>The entity tag is an MD5 hash of that version of the object.</p>
     */
    String eTag;

    /**
     * <p>Size in bytes of the object.</p>
     */
    Integer size;

    /**
     * <p>The class of storage used to store the object.</p>
     */
    ObjectVersionStorageClass storageClass;

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

    /**
     * <p>Specifies the owner of the object.</p>
     */
    Owner owner;

    ObjectVersion() {
        this.eTag = "";
        this.size = null;
        this.storageClass = null;
        this.key = "";
        this.versionId = "";
        this.isLatest = null;
        this.lastModified = null;
        this.owner = null;
    }

    protected ObjectVersion(BuilderImpl builder) {
        this.eTag = builder.eTag;
        this.size = builder.size;
        this.storageClass = builder.storageClass;
        this.key = builder.key;
        this.versionId = builder.versionId;
        this.isLatest = builder.isLatest;
        this.lastModified = builder.lastModified;
        this.owner = builder.owner;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public Integer size() {
        return size;
    }

    public ObjectVersionStorageClass storageClass() {
        return storageClass;
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

    public Owner owner() {
        return owner;
    }

    public void setETag(final String eTag) {
        this.eTag = eTag;
    }

    public void setSize(final Integer size) {
        this.size = size;
    }

    public void setStorageClass(final ObjectVersionStorageClass storageClass) {
        this.storageClass = storageClass;
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

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    public interface Builder {
        Builder eTag(String eTag);

        Builder size(Integer size);

        Builder storageClass(ObjectVersionStorageClass storageClass);

        Builder key(String key);

        Builder versionId(String versionId);

        Builder isLatest(Boolean isLatest);

        Builder lastModified(Instant lastModified);

        Builder owner(Owner owner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The entity tag is an MD5 hash of that version of the object.</p>
         */
        String eTag;

        /**
         * <p>Size in bytes of the object.</p>
         */
        Integer size;

        /**
         * <p>The class of storage used to store the object.</p>
         */
        ObjectVersionStorageClass storageClass;

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

        /**
         * <p>Specifies the owner of the object.</p>
         */
        Owner owner;

        protected BuilderImpl() {
        }

        private BuilderImpl(ObjectVersion model) {
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
            return new ObjectVersion(this);
        }

        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        public final Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public final Builder storageClass(ObjectVersionStorageClass storageClass) {
            this.storageClass = storageClass;
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

        public final Builder owner(Owner owner) {
            this.owner = owner;
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

        public String eTag() {
            return eTag;
        }

        public Integer size() {
            return size;
        }

        public ObjectVersionStorageClass storageClass() {
            return storageClass;
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

        public Owner owner() {
            return owner;
        }

        public void setETag(final String eTag) {
            this.eTag = eTag;
        }

        public void setSize(final Integer size) {
            this.size = size;
        }

        public void setStorageClass(final ObjectVersionStorageClass storageClass) {
            this.storageClass = storageClass;
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

        public void setOwner(final Owner owner) {
            this.owner = owner;
        }
    }
}
