// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class S3Location {
    /**
     * <p>The name of the bucket where the restore results will be placed.</p>
     */
    String bucketName;

    /**
     * <p>The prefix that is prepended to the restore results for this request.</p>
     */
    String prefix;

    Encryption encryption;

    /**
     * <p>The canned ACL to apply to the restore results.</p>
     */
    ObjectCannedACL cannedACL;

    /**
     * <p>A list of grants that control access to the staged results.</p>
     */
    List<Grant> accessControlList;

    /**
     * <p>The tag-set that is applied to the restore results.</p>
     */
    Tagging tagging;

    /**
     * <p>A list of metadata to store with the restore results in S3.</p>
     */
    List<MetadataEntry> userMetadata;

    /**
     * <p>The class of storage used to store the restore results.</p>
     */
    StorageClass storageClass;

    S3Location() {
        this.bucketName = "";
        this.prefix = "";
        this.encryption = null;
        this.cannedACL = null;
        this.accessControlList = null;
        this.tagging = null;
        this.userMetadata = null;
        this.storageClass = null;
    }

    protected S3Location(BuilderImpl builder) {
        this.bucketName = builder.bucketName;
        this.prefix = builder.prefix;
        this.encryption = builder.encryption;
        this.cannedACL = builder.cannedACL;
        this.accessControlList = builder.accessControlList;
        this.tagging = builder.tagging;
        this.userMetadata = builder.userMetadata;
        this.storageClass = builder.storageClass;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String prefix() {
        return prefix;
    }

    public Encryption encryption() {
        return encryption;
    }

    public ObjectCannedACL cannedACL() {
        return cannedACL;
    }

    public List<Grant> accessControlList() {
        return accessControlList;
    }

    public Tagging tagging() {
        return tagging;
    }

    public List<MetadataEntry> userMetadata() {
        return userMetadata;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public void setBucketName(final String bucketName) {
        this.bucketName = bucketName;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setEncryption(final Encryption encryption) {
        this.encryption = encryption;
    }

    public void setCannedACL(final ObjectCannedACL cannedACL) {
        this.cannedACL = cannedACL;
    }

    public void setAccessControlList(final List<Grant> accessControlList) {
        this.accessControlList = accessControlList;
    }

    public void setTagging(final Tagging tagging) {
        this.tagging = tagging;
    }

    public void setUserMetadata(final List<MetadataEntry> userMetadata) {
        this.userMetadata = userMetadata;
    }

    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public interface Builder {
        Builder bucketName(String bucketName);

        Builder prefix(String prefix);

        Builder encryption(Encryption encryption);

        Builder cannedACL(ObjectCannedACL cannedACL);

        Builder accessControlList(List<Grant> accessControlList);

        Builder tagging(Tagging tagging);

        Builder userMetadata(List<MetadataEntry> userMetadata);

        Builder storageClass(StorageClass storageClass);

        S3Location build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket where the restore results will be placed.</p>
         */
        String bucketName;

        /**
         * <p>The prefix that is prepended to the restore results for this request.</p>
         */
        String prefix;

        Encryption encryption;

        /**
         * <p>The canned ACL to apply to the restore results.</p>
         */
        ObjectCannedACL cannedACL;

        /**
         * <p>A list of grants that control access to the staged results.</p>
         */
        List<Grant> accessControlList;

        /**
         * <p>The tag-set that is applied to the restore results.</p>
         */
        Tagging tagging;

        /**
         * <p>A list of metadata to store with the restore results in S3.</p>
         */
        List<MetadataEntry> userMetadata;

        /**
         * <p>The class of storage used to store the restore results.</p>
         */
        StorageClass storageClass;

        protected BuilderImpl() {
        }

        private BuilderImpl(S3Location model) {
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
            return new S3Location(this);
        }

        public final Builder bucketName(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder encryption(Encryption encryption) {
            this.encryption = encryption;
            return this;
        }

        public final Builder cannedACL(ObjectCannedACL cannedACL) {
            this.cannedACL = cannedACL;
            return this;
        }

        public final Builder accessControlList(List<Grant> accessControlList) {
            this.accessControlList = accessControlList;
            return this;
        }

        public final Builder tagging(Tagging tagging) {
            this.tagging = tagging;
            return this;
        }

        public final Builder userMetadata(List<MetadataEntry> userMetadata) {
            this.userMetadata = userMetadata;
            return this;
        }

        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
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

        public String bucketName() {
            return bucketName;
        }

        public String prefix() {
            return prefix;
        }

        public Encryption encryption() {
            return encryption;
        }

        public ObjectCannedACL cannedACL() {
            return cannedACL;
        }

        public List<Grant> accessControlList() {
            return accessControlList;
        }

        public Tagging tagging() {
            return tagging;
        }

        public List<MetadataEntry> userMetadata() {
            return userMetadata;
        }

        public StorageClass storageClass() {
            return storageClass;
        }

        public void setBucketName(final String bucketName) {
            this.bucketName = bucketName;
        }

        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }

        public void setEncryption(final Encryption encryption) {
            this.encryption = encryption;
        }

        public void setCannedACL(final ObjectCannedACL cannedACL) {
            this.cannedACL = cannedACL;
        }

        public void setAccessControlList(final List<Grant> accessControlList) {
            this.accessControlList = accessControlList;
        }

        public void setTagging(final Tagging tagging) {
            this.tagging = tagging;
        }

        public void setUserMetadata(final List<MetadataEntry> userMetadata) {
            this.userMetadata = userMetadata;
        }

        public void setStorageClass(final StorageClass storageClass) {
            this.storageClass = storageClass;
        }
    }
}
