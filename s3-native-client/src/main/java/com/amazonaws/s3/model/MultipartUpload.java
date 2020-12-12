// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class MultipartUpload {
    private String uploadId;

    private String key;

    private Instant initiated;

    private StorageClass storageClass;

    private Owner owner;

    private Initiator initiator;

    private MultipartUpload() {
        this.uploadId = null;
        this.key = null;
        this.initiated = null;
        this.storageClass = null;
        this.owner = null;
        this.initiator = null;
    }

    private MultipartUpload(Builder builder) {
        this.uploadId = builder.uploadId;
        this.key = builder.key;
        this.initiated = builder.initiated;
        this.storageClass = builder.storageClass;
        this.owner = builder.owner;
        this.initiator = builder.initiator;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(MultipartUpload.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof MultipartUpload);
    }

    public String uploadId() {
        return uploadId;
    }

    public void setUploadId(final String uploadId) {
        this.uploadId = uploadId;
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public Instant initiated() {
        return initiated;
    }

    public void setInitiated(final Instant initiated) {
        this.initiated = initiated;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public Owner owner() {
        return owner;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    public Initiator initiator() {
        return initiator;
    }

    public void setInitiator(final Initiator initiator) {
        this.initiator = initiator;
    }

    static final class Builder {
        private String uploadId;

        private String key;

        private Instant initiated;

        private StorageClass storageClass;

        private Owner owner;

        private Initiator initiator;

        private Builder() {
        }

        private Builder(MultipartUpload model) {
            uploadId(model.uploadId);
            key(model.key);
            initiated(model.initiated);
            storageClass(model.storageClass);
            owner(model.owner);
            initiator(model.initiator);
        }

        public MultipartUpload build() {
            return new com.amazonaws.s3.model.MultipartUpload(this);
        }

        /**
         * <p>Upload ID that identifies the multipart upload.</p>
         */
        public final Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        /**
         * <p>Key of the object for which the multipart upload was initiated.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>Date and time at which the multipart upload was initiated.</p>
         */
        public final Builder initiated(Instant initiated) {
            this.initiated = initiated;
            return this;
        }

        /**
         * <p>The class of storage used to store the object.</p>
         */
        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        /**
         * <p>Specifies the owner of the object that is part of the multipart upload. </p>
         */
        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }

        /**
         * <p>Identifies who initiated the multipart upload.</p>
         */
        public final Builder initiator(Initiator initiator) {
            this.initiator = initiator;
            return this;
        }
    }
}
