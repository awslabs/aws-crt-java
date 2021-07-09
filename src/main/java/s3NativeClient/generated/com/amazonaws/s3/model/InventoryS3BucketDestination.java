// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InventoryS3BucketDestination {
    /**
     * <p>The account ID that owns the destination S3 bucket. If no account ID is provided, the
     *          owner is not validated before exporting data. </p>
     *          <note>
     *             <p> Although this value is optional, we strongly recommend that you set it to help
     *             prevent problems if the destination bucket ownership changes. </p>
     *          </note>
     */
    String accountId;

    /**
     * <p>The Amazon Resource Name (ARN) of the bucket where inventory results will be
     *          published.</p>
     */
    String bucket;

    /**
     * <p>Specifies the output format of the inventory results.</p>
     */
    InventoryFormat format;

    /**
     * <p>The prefix that is prepended to all inventory results.</p>
     */
    String prefix;

    /**
     * <p>Contains the type of server-side encryption used to encrypt the inventory
     *          results.</p>
     */
    InventoryEncryption encryption;

    InventoryS3BucketDestination() {
        this.accountId = "";
        this.bucket = "";
        this.format = null;
        this.prefix = "";
        this.encryption = null;
    }

    protected InventoryS3BucketDestination(BuilderImpl builder) {
        this.accountId = builder.accountId;
        this.bucket = builder.bucket;
        this.format = builder.format;
        this.prefix = builder.prefix;
        this.encryption = builder.encryption;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String bucket() {
        return bucket;
    }

    public InventoryFormat format() {
        return format;
    }

    public String prefix() {
        return prefix;
    }

    public InventoryEncryption encryption() {
        return encryption;
    }

    public interface Builder {
        Builder accountId(String accountId);

        Builder bucket(String bucket);

        Builder format(InventoryFormat format);

        Builder prefix(String prefix);

        Builder encryption(InventoryEncryption encryption);

        InventoryS3BucketDestination build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The account ID that owns the destination S3 bucket. If no account ID is provided, the
         *          owner is not validated before exporting data. </p>
         *          <note>
         *             <p> Although this value is optional, we strongly recommend that you set it to help
         *             prevent problems if the destination bucket ownership changes. </p>
         *          </note>
         */
        String accountId;

        /**
         * <p>The Amazon Resource Name (ARN) of the bucket where inventory results will be
         *          published.</p>
         */
        String bucket;

        /**
         * <p>Specifies the output format of the inventory results.</p>
         */
        InventoryFormat format;

        /**
         * <p>The prefix that is prepended to all inventory results.</p>
         */
        String prefix;

        /**
         * <p>Contains the type of server-side encryption used to encrypt the inventory
         *          results.</p>
         */
        InventoryEncryption encryption;

        protected BuilderImpl() {
        }

        private BuilderImpl(InventoryS3BucketDestination model) {
            accountId(model.accountId);
            bucket(model.bucket);
            format(model.format);
            prefix(model.prefix);
            encryption(model.encryption);
        }

        public InventoryS3BucketDestination build() {
            return new InventoryS3BucketDestination(this);
        }

        public final Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder format(InventoryFormat format) {
            this.format = format;
            return this;
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder encryption(InventoryEncryption encryption) {
            this.encryption = encryption;
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

        public String accountId() {
            return accountId;
        }

        public String bucket() {
            return bucket;
        }

        public InventoryFormat format() {
            return format;
        }

        public String prefix() {
            return prefix;
        }

        public InventoryEncryption encryption() {
            return encryption;
        }
    }
}
