// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class S3Location {
    private String bucketName;

    private String prefix;

    private Encryption encryption;

    private ObjectCannedACL cannedACL;

    private List<Grant> accessControlList;

    private Tagging tagging;

    private List<MetadataEntry> userMetadata;

    private StorageClass storageClass;

    private S3Location() {
        this.bucketName = null;
        this.prefix = null;
        this.encryption = null;
        this.cannedACL = null;
        this.accessControlList = null;
        this.tagging = null;
        this.userMetadata = null;
        this.storageClass = null;
    }

    private S3Location(Builder builder) {
        this.bucketName = builder.bucketName;
        this.prefix = builder.prefix;
        this.encryption = builder.encryption;
        this.cannedACL = builder.cannedACL;
        this.accessControlList = builder.accessControlList;
        this.tagging = builder.tagging;
        this.userMetadata = builder.userMetadata;
        this.storageClass = builder.storageClass;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(S3Location.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof S3Location);
    }

    public String bucketName() {
        return bucketName;
    }

    public void setBucketName(final String bucketName) {
        this.bucketName = bucketName;
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public Encryption encryption() {
        return encryption;
    }

    public void setEncryption(final Encryption encryption) {
        this.encryption = encryption;
    }

    public ObjectCannedACL cannedACL() {
        return cannedACL;
    }

    public void setCannedACL(final ObjectCannedACL cannedACL) {
        this.cannedACL = cannedACL;
    }

    public List<Grant> accessControlList() {
        return accessControlList;
    }

    public void setAccessControlList(final List<Grant> accessControlList) {
        this.accessControlList = accessControlList;
    }

    public Tagging tagging() {
        return tagging;
    }

    public void setTagging(final Tagging tagging) {
        this.tagging = tagging;
    }

    public List<MetadataEntry> userMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(final List<MetadataEntry> userMetadata) {
        this.userMetadata = userMetadata;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    static final class Builder {
        private String bucketName;

        private String prefix;

        private Encryption encryption;

        private ObjectCannedACL cannedACL;

        private List<Grant> accessControlList;

        private Tagging tagging;

        private List<MetadataEntry> userMetadata;

        private StorageClass storageClass;

        private Builder() {
        }

        private Builder(S3Location model) {
            bucketName(model.bucketName);
            prefix(model.prefix);
            encryption(model.encryption);
            cannedACL(model.cannedACL);
            accessControlList(model.accessControlList);
            tagging(model.tagging);
            userMetadata(model.userMetadata);
            storageClass(model.storageClass);
        }

        public S3Location build() {
            return new com.amazonaws.s3.model.S3Location(this);
        }

        /**
         * <p>The name of the bucket where the restore results will be placed.</p>
         */
        public final Builder bucketName(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        /**
         * <p>The prefix that is prepended to the restore results for this request.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder encryption(Encryption encryption) {
            this.encryption = encryption;
            return this;
        }

        /**
         * <p>The canned ACL to apply to the restore results.</p>
         */
        public final Builder cannedACL(ObjectCannedACL cannedACL) {
            this.cannedACL = cannedACL;
            return this;
        }

        /**
         * <p>A list of grants that control access to the staged results.</p>
         */
        public final Builder accessControlList(List<Grant> accessControlList) {
            this.accessControlList = accessControlList;
            return this;
        }

        /**
         * <p>The tag-set that is applied to the restore results.</p>
         */
        public final Builder tagging(Tagging tagging) {
            this.tagging = tagging;
            return this;
        }

        /**
         * <p>A list of metadata to store with the restore results in S3.</p>
         */
        public final Builder userMetadata(List<MetadataEntry> userMetadata) {
            this.userMetadata = userMetadata;
            return this;
        }

        /**
         * <p>The class of storage used to store the restore results.</p>
         */
        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }
    }
}
