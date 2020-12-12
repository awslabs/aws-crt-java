// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Object {
    private String key;

    private Instant lastModified;

    private String eTag;

    private Integer size;

    private ObjectStorageClass storageClass;

    private Owner owner;

    private Object() {
        this.key = null;
        this.lastModified = null;
        this.eTag = null;
        this.size = null;
        this.storageClass = null;
        this.owner = null;
    }

    private Object(Builder builder) {
        this.key = builder.key;
        this.lastModified = builder.lastModified;
        this.eTag = builder.eTag;
        this.size = builder.size;
        this.storageClass = builder.storageClass;
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
        return Objects.hash(Object.class);
    }

    @Override
    public boolean equals(java.lang.Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Object);
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public Instant lastModified() {
        return lastModified;
    }

    public void setLastModified(final Instant lastModified) {
        this.lastModified = lastModified;
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

    public ObjectStorageClass storageClass() {
        return storageClass;
    }

    public void setStorageClass(final ObjectStorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public Owner owner() {
        return owner;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    static final class Builder {
        private String key;

        private Instant lastModified;

        private String eTag;

        private Integer size;

        private ObjectStorageClass storageClass;

        private Owner owner;

        private Builder() {
        }

        private Builder(Object model) {
            key(model.key);
            lastModified(model.lastModified);
            eTag(model.eTag);
            size(model.size);
            storageClass(model.storageClass);
            owner(model.owner);
        }

        public Object build() {
            return new com.amazonaws.s3.model.Object(this);
        }

        /**
         * <p>The name that you assign to an object. You use the object key to retrieve the
         *          object.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>The date the Object was Last Modified</p>
         */
        public final Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        /**
         * <p>The entity tag is a hash of the object. The ETag reflects changes only to the contents
         *          of an object, not its metadata. The ETag may or may not be an MD5 digest of the object
         *          data. Whether or not it is depends on how the object was created and how it is encrypted as
         *          described below:</p>
         *          <ul>
         *             <li>
         *                <p>Objects created by the PUT Object, POST Object, or Copy operation, or through the
         *                AWS Management Console, and are encrypted by SSE-S3 or plaintext, have ETags that are
         *                an MD5 digest of their object data.</p>
         *             </li>
         *             <li>
         *                <p>Objects created by the PUT Object, POST Object, or Copy operation, or through the
         *                AWS Management Console, and are encrypted by SSE-C or SSE-KMS, have ETags that are
         *                not an MD5 digest of their object data.</p>
         *             </li>
         *             <li>
         *                <p>If an object is created by either the Multipart Upload or Part Copy operation, the
         *                ETag is not an MD5 digest, regardless of the method of encryption.</p>
         *             </li>
         *          </ul>
         */
        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        /**
         * <p>Size in bytes of the object</p>
         */
        public final Builder size(Integer size) {
            this.size = size;
            return this;
        }

        /**
         * <p>The class of storage used to store the object.</p>
         */
        public final Builder storageClass(ObjectStorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        /**
         * <p>The owner of the object</p>
         */
        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }
    }
}
