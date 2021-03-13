// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class MultipartUpload {
    /**
     * <p>Upload ID that identifies the multipart upload.</p>
     */
    String uploadId;

    /**
     * <p>Key of the object for which the multipart upload was initiated.</p>
     */
    String key;

    /**
     * <p>Date and time at which the multipart upload was initiated.</p>
     */
    Instant initiated;

    /**
     * <p>The class of storage used to store the object.</p>
     */
    StorageClass storageClass;

    /**
     * <p>Specifies the owner of the object that is part of the multipart upload. </p>
     */
    Owner owner;

    /**
     * <p>Identifies who initiated the multipart upload.</p>
     */
    Initiator initiator;

    MultipartUpload() {
        this.uploadId = "";
        this.key = "";
        this.initiated = null;
        this.storageClass = null;
        this.owner = null;
        this.initiator = null;
    }

    protected MultipartUpload(BuilderImpl builder) {
        this.uploadId = builder.uploadId;
        this.key = builder.key;
        this.initiated = builder.initiated;
        this.storageClass = builder.storageClass;
        this.owner = builder.owner;
        this.initiator = builder.initiator;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String key() {
        return key;
    }

    public Instant initiated() {
        return initiated;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public Owner owner() {
        return owner;
    }

    public Initiator initiator() {
        return initiator;
    }

    public interface Builder {
        Builder uploadId(String uploadId);

        Builder key(String key);

        Builder initiated(Instant initiated);

        Builder storageClass(StorageClass storageClass);

        Builder owner(Owner owner);

        Builder initiator(Initiator initiator);

        MultipartUpload build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Upload ID that identifies the multipart upload.</p>
         */
        String uploadId;

        /**
         * <p>Key of the object for which the multipart upload was initiated.</p>
         */
        String key;

        /**
         * <p>Date and time at which the multipart upload was initiated.</p>
         */
        Instant initiated;

        /**
         * <p>The class of storage used to store the object.</p>
         */
        StorageClass storageClass;

        /**
         * <p>Specifies the owner of the object that is part of the multipart upload. </p>
         */
        Owner owner;

        /**
         * <p>Identifies who initiated the multipart upload.</p>
         */
        Initiator initiator;

        protected BuilderImpl() {
        }

        private BuilderImpl(MultipartUpload model) {
            uploadId(model.uploadId);
            key(model.key);
            initiated(model.initiated);
            storageClass(model.storageClass);
            owner(model.owner);
            initiator(model.initiator);
        }

        public MultipartUpload build() {
            return new MultipartUpload(this);
        }

        public final Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder initiated(Instant initiated) {
            this.initiated = initiated;
            return this;
        }

        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }

        public final Builder initiator(Initiator initiator) {
            this.initiator = initiator;
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

        public String uploadId() {
            return uploadId;
        }

        public String key() {
            return key;
        }

        public Instant initiated() {
            return initiated;
        }

        public StorageClass storageClass() {
            return storageClass;
        }

        public Owner owner() {
            return owner;
        }

        public Initiator initiator() {
            return initiator;
        }
    }
}
