// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ServerSideEncryptionRule {
    /**
     * <p>Specifies the default server-side encryption to apply to new objects in the bucket. If a
     *          PUT Object request doesn't specify any server-side encryption, this default encryption will
     *          be applied.</p>
     */
    ServerSideEncryptionByDefault applyServerSideEncryptionByDefault;

    /**
     * <p>Specifies whether Amazon S3 should use an S3 Bucket Key with server-side encryption using KMS (SSE-KMS) for new objects in the bucket. Existing objects are not affected. Setting the <code>BucketKeyEnabled</code> element to <code>true</code> causes Amazon S3 to use an S3 Bucket Key. By default, S3 Bucket Key is not enabled.</p>
     *          <p>For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/bucket-key.html">Amazon S3 Bucket Keys</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    Boolean bucketKeyEnabled;

    ServerSideEncryptionRule() {
        this.applyServerSideEncryptionByDefault = null;
        this.bucketKeyEnabled = null;
    }

    protected ServerSideEncryptionRule(BuilderImpl builder) {
        this.applyServerSideEncryptionByDefault = builder.applyServerSideEncryptionByDefault;
        this.bucketKeyEnabled = builder.bucketKeyEnabled;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ServerSideEncryptionRule.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ServerSideEncryptionRule);
    }

    public ServerSideEncryptionByDefault applyServerSideEncryptionByDefault() {
        return applyServerSideEncryptionByDefault;
    }

    public Boolean bucketKeyEnabled() {
        return bucketKeyEnabled;
    }

    public void setApplyServerSideEncryptionByDefault(
            final ServerSideEncryptionByDefault applyServerSideEncryptionByDefault) {
        this.applyServerSideEncryptionByDefault = applyServerSideEncryptionByDefault;
    }

    public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
        this.bucketKeyEnabled = bucketKeyEnabled;
    }

    public interface Builder {
        Builder applyServerSideEncryptionByDefault(
                ServerSideEncryptionByDefault applyServerSideEncryptionByDefault);

        Builder bucketKeyEnabled(Boolean bucketKeyEnabled);

        ServerSideEncryptionRule build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the default server-side encryption to apply to new objects in the bucket. If a
         *          PUT Object request doesn't specify any server-side encryption, this default encryption will
         *          be applied.</p>
         */
        ServerSideEncryptionByDefault applyServerSideEncryptionByDefault;

        /**
         * <p>Specifies whether Amazon S3 should use an S3 Bucket Key with server-side encryption using KMS (SSE-KMS) for new objects in the bucket. Existing objects are not affected. Setting the <code>BucketKeyEnabled</code> element to <code>true</code> causes Amazon S3 to use an S3 Bucket Key. By default, S3 Bucket Key is not enabled.</p>
         *          <p>For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/bucket-key.html">Amazon S3 Bucket Keys</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        Boolean bucketKeyEnabled;

        protected BuilderImpl() {
        }

        private BuilderImpl(ServerSideEncryptionRule model) {
            applyServerSideEncryptionByDefault(model.applyServerSideEncryptionByDefault);
            bucketKeyEnabled(model.bucketKeyEnabled);
        }

        public ServerSideEncryptionRule build() {
            return new ServerSideEncryptionRule(this);
        }

        public final Builder applyServerSideEncryptionByDefault(
                ServerSideEncryptionByDefault applyServerSideEncryptionByDefault) {
            this.applyServerSideEncryptionByDefault = applyServerSideEncryptionByDefault;
            return this;
        }

        public final Builder bucketKeyEnabled(Boolean bucketKeyEnabled) {
            this.bucketKeyEnabled = bucketKeyEnabled;
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

        public ServerSideEncryptionByDefault applyServerSideEncryptionByDefault() {
            return applyServerSideEncryptionByDefault;
        }

        public Boolean bucketKeyEnabled() {
            return bucketKeyEnabled;
        }

        public void setApplyServerSideEncryptionByDefault(
                final ServerSideEncryptionByDefault applyServerSideEncryptionByDefault) {
            this.applyServerSideEncryptionByDefault = applyServerSideEncryptionByDefault;
        }

        public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
            this.bucketKeyEnabled = bucketKeyEnabled;
        }
    }
}
