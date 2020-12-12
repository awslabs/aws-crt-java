// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Destination {
    private String bucket;

    private String account;

    private StorageClass storageClass;

    private AccessControlTranslation accessControlTranslation;

    private EncryptionConfiguration encryptionConfiguration;

    private ReplicationTime replicationTime;

    private Metrics metrics;

    private Destination() {
        this.bucket = null;
        this.account = null;
        this.storageClass = null;
        this.accessControlTranslation = null;
        this.encryptionConfiguration = null;
        this.replicationTime = null;
        this.metrics = null;
    }

    private Destination(Builder builder) {
        this.bucket = builder.bucket;
        this.account = builder.account;
        this.storageClass = builder.storageClass;
        this.accessControlTranslation = builder.accessControlTranslation;
        this.encryptionConfiguration = builder.encryptionConfiguration;
        this.replicationTime = builder.replicationTime;
        this.metrics = builder.metrics;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String account() {
        return account;
    }

    public void setAccount(final String account) {
        this.account = account;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public AccessControlTranslation accessControlTranslation() {
        return accessControlTranslation;
    }

    public void setAccessControlTranslation(
            final AccessControlTranslation accessControlTranslation) {
        this.accessControlTranslation = accessControlTranslation;
    }

    public EncryptionConfiguration encryptionConfiguration() {
        return encryptionConfiguration;
    }

    public void setEncryptionConfiguration(final EncryptionConfiguration encryptionConfiguration) {
        this.encryptionConfiguration = encryptionConfiguration;
    }

    public ReplicationTime replicationTime() {
        return replicationTime;
    }

    public void setReplicationTime(final ReplicationTime replicationTime) {
        this.replicationTime = replicationTime;
    }

    public Metrics metrics() {
        return metrics;
    }

    public void setMetrics(final Metrics metrics) {
        this.metrics = metrics;
    }

    static final class Builder {
        private String bucket;

        private String account;

        private StorageClass storageClass;

        private AccessControlTranslation accessControlTranslation;

        private EncryptionConfiguration encryptionConfiguration;

        private ReplicationTime replicationTime;

        private Metrics metrics;

        private Builder() {
        }

        private Builder(Destination model) {
            bucket(model.bucket);
            account(model.account);
            storageClass(model.storageClass);
            accessControlTranslation(model.accessControlTranslation);
            encryptionConfiguration(model.encryptionConfiguration);
            replicationTime(model.replicationTime);
            metrics(model.metrics);
        }

        public Destination build() {
            return new com.amazonaws.s3.model.Destination(this);
        }

        /**
         * <p> The Amazon Resource Name (ARN) of the bucket where you want Amazon S3 to store the
         *          results.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Destination bucket owner account ID. In a cross-account scenario, if you direct Amazon S3 to
         *          change replica ownership to the AWS account that owns the destination bucket by specifying
         *          the <code>AccessControlTranslation</code> property, this is the account ID of the
         *          destination bucket owner. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-change-owner.html">Replication Additional
         *             Configuration: Changing the Replica Owner</a> in the <i>Amazon Simple Storage
         *             Service Developer Guide</i>.</p>
         */
        public final Builder account(String account) {
            this.account = account;
            return this;
        }

        /**
         * <p> The storage class to use when replicating objects, such as S3 Standard or reduced
         *          redundancy. By default, Amazon S3 uses the storage class of the source object to create the
         *          object replica. </p>
         *          <p>For valid values, see the <code>StorageClass</code> element of the <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTreplication.html">PUT Bucket
         *             replication</a> action in the <i>Amazon Simple Storage Service API Reference</i>.</p>
         */
        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        /**
         * <p>Specify this only in a cross-account scenario (where source and destination bucket
         *          owners are not the same), and you want to change replica ownership to the AWS account that
         *          owns the destination bucket. If this is not specified in the replication configuration, the
         *          replicas are owned by same AWS account that owns the source object.</p>
         */
        public final Builder accessControlTranslation(
                AccessControlTranslation accessControlTranslation) {
            this.accessControlTranslation = accessControlTranslation;
            return this;
        }

        /**
         * <p>A container that provides information about encryption. If
         *             <code>SourceSelectionCriteria</code> is specified, you must specify this element.</p>
         */
        public final Builder encryptionConfiguration(
                EncryptionConfiguration encryptionConfiguration) {
            this.encryptionConfiguration = encryptionConfiguration;
            return this;
        }

        /**
         * <p> A container specifying S3 Replication Time Control (S3 RTC), including whether S3 RTC is enabled and the time
         *          when all objects and operations on objects must be replicated. Must be specified together
         *          with a <code>Metrics</code> block. </p>
         */
        public final Builder replicationTime(ReplicationTime replicationTime) {
            this.replicationTime = replicationTime;
            return this;
        }

        /**
         * <p> A container specifying replication metrics-related settings enabling replication
         *          metrics and events. </p>
         */
        public final Builder metrics(Metrics metrics) {
            this.metrics = metrics;
            return this;
        }
    }
}
