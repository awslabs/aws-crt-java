// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Destination {
    /**
     * <p> The Amazon Resource Name (ARN) of the bucket where you want Amazon S3 to store the
     *          results.</p>
     */
    String bucket;

    /**
     * <p>Destination bucket owner account ID. In a cross-account scenario, if you direct Amazon S3 to
     *          change replica ownership to the AWS account that owns the destination bucket by specifying
     *          the <code>AccessControlTranslation</code> property, this is the account ID of the
     *          destination bucket owner. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-change-owner.html">Replication Additional
     *             Configuration: Changing the Replica Owner</a> in the <i>Amazon Simple Storage
     *             Service Developer Guide</i>.</p>
     */
    String account;

    /**
     * <p> The storage class to use when replicating objects, such as S3 Standard or reduced
     *          redundancy. By default, Amazon S3 uses the storage class of the source object to create the
     *          object replica. </p>
     *          <p>For valid values, see the <code>StorageClass</code> element of the <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTreplication.html">PUT Bucket
     *             replication</a> action in the <i>Amazon Simple Storage Service API Reference</i>.</p>
     */
    StorageClass storageClass;

    /**
     * <p>Specify this only in a cross-account scenario (where source and destination bucket
     *          owners are not the same), and you want to change replica ownership to the AWS account that
     *          owns the destination bucket. If this is not specified in the replication configuration, the
     *          replicas are owned by same AWS account that owns the source object.</p>
     */
    AccessControlTranslation accessControlTranslation;

    /**
     * <p>A container that provides information about encryption. If
     *             <code>SourceSelectionCriteria</code> is specified, you must specify this element.</p>
     */
    EncryptionConfiguration encryptionConfiguration;

    /**
     * <p> A container specifying S3 Replication Time Control (S3 RTC), including whether S3 RTC is enabled and the time
     *          when all objects and operations on objects must be replicated. Must be specified together
     *          with a <code>Metrics</code> block. </p>
     */
    ReplicationTime replicationTime;

    /**
     * <p> A container specifying replication metrics-related settings enabling replication
     *          metrics and events. </p>
     */
    Metrics metrics;

    Destination() {
        this.bucket = "";
        this.account = "";
        this.storageClass = null;
        this.accessControlTranslation = null;
        this.encryptionConfiguration = null;
        this.replicationTime = null;
        this.metrics = null;
    }

    protected Destination(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.account = builder.account;
        this.storageClass = builder.storageClass;
        this.accessControlTranslation = builder.accessControlTranslation;
        this.encryptionConfiguration = builder.encryptionConfiguration;
        this.replicationTime = builder.replicationTime;
        this.metrics = builder.metrics;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(Destination.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Destination);
    }

    public String bucket() {
        return bucket;
    }

    public String account() {
        return account;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public AccessControlTranslation accessControlTranslation() {
        return accessControlTranslation;
    }

    public EncryptionConfiguration encryptionConfiguration() {
        return encryptionConfiguration;
    }

    public ReplicationTime replicationTime() {
        return replicationTime;
    }

    public Metrics metrics() {
        return metrics;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setAccount(final String account) {
        this.account = account;
    }

    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public void setAccessControlTranslation(
            final AccessControlTranslation accessControlTranslation) {
        this.accessControlTranslation = accessControlTranslation;
    }

    public void setEncryptionConfiguration(final EncryptionConfiguration encryptionConfiguration) {
        this.encryptionConfiguration = encryptionConfiguration;
    }

    public void setReplicationTime(final ReplicationTime replicationTime) {
        this.replicationTime = replicationTime;
    }

    public void setMetrics(final Metrics metrics) {
        this.metrics = metrics;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder account(String account);

        Builder storageClass(StorageClass storageClass);

        Builder accessControlTranslation(AccessControlTranslation accessControlTranslation);

        Builder encryptionConfiguration(EncryptionConfiguration encryptionConfiguration);

        Builder replicationTime(ReplicationTime replicationTime);

        Builder metrics(Metrics metrics);

        Destination build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p> The Amazon Resource Name (ARN) of the bucket where you want Amazon S3 to store the
         *          results.</p>
         */
        String bucket;

        /**
         * <p>Destination bucket owner account ID. In a cross-account scenario, if you direct Amazon S3 to
         *          change replica ownership to the AWS account that owns the destination bucket by specifying
         *          the <code>AccessControlTranslation</code> property, this is the account ID of the
         *          destination bucket owner. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-change-owner.html">Replication Additional
         *             Configuration: Changing the Replica Owner</a> in the <i>Amazon Simple Storage
         *             Service Developer Guide</i>.</p>
         */
        String account;

        /**
         * <p> The storage class to use when replicating objects, such as S3 Standard or reduced
         *          redundancy. By default, Amazon S3 uses the storage class of the source object to create the
         *          object replica. </p>
         *          <p>For valid values, see the <code>StorageClass</code> element of the <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTreplication.html">PUT Bucket
         *             replication</a> action in the <i>Amazon Simple Storage Service API Reference</i>.</p>
         */
        StorageClass storageClass;

        /**
         * <p>Specify this only in a cross-account scenario (where source and destination bucket
         *          owners are not the same), and you want to change replica ownership to the AWS account that
         *          owns the destination bucket. If this is not specified in the replication configuration, the
         *          replicas are owned by same AWS account that owns the source object.</p>
         */
        AccessControlTranslation accessControlTranslation;

        /**
         * <p>A container that provides information about encryption. If
         *             <code>SourceSelectionCriteria</code> is specified, you must specify this element.</p>
         */
        EncryptionConfiguration encryptionConfiguration;

        /**
         * <p> A container specifying S3 Replication Time Control (S3 RTC), including whether S3 RTC is enabled and the time
         *          when all objects and operations on objects must be replicated. Must be specified together
         *          with a <code>Metrics</code> block. </p>
         */
        ReplicationTime replicationTime;

        /**
         * <p> A container specifying replication metrics-related settings enabling replication
         *          metrics and events. </p>
         */
        Metrics metrics;

        protected BuilderImpl() {
        }

        private BuilderImpl(Destination model) {
            bucket(model.bucket);
            account(model.account);
            storageClass(model.storageClass);
            accessControlTranslation(model.accessControlTranslation);
            encryptionConfiguration(model.encryptionConfiguration);
            replicationTime(model.replicationTime);
            metrics(model.metrics);
        }

        public Destination build() {
            return new Destination(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder account(String account) {
            this.account = account;
            return this;
        }

        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        public final Builder accessControlTranslation(
                AccessControlTranslation accessControlTranslation) {
            this.accessControlTranslation = accessControlTranslation;
            return this;
        }

        public final Builder encryptionConfiguration(
                EncryptionConfiguration encryptionConfiguration) {
            this.encryptionConfiguration = encryptionConfiguration;
            return this;
        }

        public final Builder replicationTime(ReplicationTime replicationTime) {
            this.replicationTime = replicationTime;
            return this;
        }

        public final Builder metrics(Metrics metrics) {
            this.metrics = metrics;
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

        public String bucket() {
            return bucket;
        }

        public String account() {
            return account;
        }

        public StorageClass storageClass() {
            return storageClass;
        }

        public AccessControlTranslation accessControlTranslation() {
            return accessControlTranslation;
        }

        public EncryptionConfiguration encryptionConfiguration() {
            return encryptionConfiguration;
        }

        public ReplicationTime replicationTime() {
            return replicationTime;
        }

        public Metrics metrics() {
            return metrics;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setAccount(final String account) {
            this.account = account;
        }

        public void setStorageClass(final StorageClass storageClass) {
            this.storageClass = storageClass;
        }

        public void setAccessControlTranslation(
                final AccessControlTranslation accessControlTranslation) {
            this.accessControlTranslation = accessControlTranslation;
        }

        public void setEncryptionConfiguration(
                final EncryptionConfiguration encryptionConfiguration) {
            this.encryptionConfiguration = encryptionConfiguration;
        }

        public void setReplicationTime(final ReplicationTime replicationTime) {
            this.replicationTime = replicationTime;
        }

        public void setMetrics(final Metrics metrics) {
            this.metrics = metrics;
        }
    }
}
