// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InventoryS3BucketDestination {
    private String accountId;

    private String bucket;

    private InventoryFormat format;

    private String prefix;

    private InventoryEncryption encryption;

    private InventoryS3BucketDestination() {
        this.accountId = null;
        this.bucket = null;
        this.format = null;
        this.prefix = null;
        this.encryption = null;
    }

    private InventoryS3BucketDestination(Builder builder) {
        this.accountId = builder.accountId;
        this.bucket = builder.bucket;
        this.format = builder.format;
        this.prefix = builder.prefix;
        this.encryption = builder.encryption;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(InventoryS3BucketDestination.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof InventoryS3BucketDestination);
    }

    public String accountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public InventoryFormat format() {
        return format;
    }

    public void setFormat(final InventoryFormat format) {
        this.format = format;
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public InventoryEncryption encryption() {
        return encryption;
    }

    public void setEncryption(final InventoryEncryption encryption) {
        this.encryption = encryption;
    }

    static final class Builder {
        private String accountId;

        private String bucket;

        private InventoryFormat format;

        private String prefix;

        private InventoryEncryption encryption;

        private Builder() {
        }

        private Builder(InventoryS3BucketDestination model) {
            accountId(model.accountId);
            bucket(model.bucket);
            format(model.format);
            prefix(model.prefix);
            encryption(model.encryption);
        }

        public InventoryS3BucketDestination build() {
            return new com.amazonaws.s3.model.InventoryS3BucketDestination(this);
        }

        /**
         * <p>The account ID that owns the destination S3 bucket. If no account ID is provided, the
         *          owner is not validated before exporting data. </p>
         *          <note>
         *             <p> Although this value is optional, we strongly recommend that you set it to help
         *             prevent problems if the destination bucket ownership changes. </p>
         *          </note>
         */
        public final Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        /**
         * <p>The Amazon Resource Name (ARN) of the bucket where inventory results will be
         *          published.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Specifies the output format of the inventory results.</p>
         */
        public final Builder format(InventoryFormat format) {
            this.format = format;
            return this;
        }

        /**
         * <p>The prefix that is prepended to all inventory results.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>Contains the type of server-side encryption used to encrypt the inventory
         *          results.</p>
         */
        public final Builder encryption(InventoryEncryption encryption) {
            this.encryption = encryption;
            return this;
        }
    }
}
