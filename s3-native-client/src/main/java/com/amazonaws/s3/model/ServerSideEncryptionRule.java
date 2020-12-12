// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ServerSideEncryptionRule {
    private ServerSideEncryptionByDefault applyServerSideEncryptionByDefault;

    private Boolean bucketKeyEnabled;

    private ServerSideEncryptionRule() {
        this.applyServerSideEncryptionByDefault = null;
        this.bucketKeyEnabled = null;
    }

    private ServerSideEncryptionRule(Builder builder) {
        this.applyServerSideEncryptionByDefault = builder.applyServerSideEncryptionByDefault;
        this.bucketKeyEnabled = builder.bucketKeyEnabled;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setApplyServerSideEncryptionByDefault(
            final ServerSideEncryptionByDefault applyServerSideEncryptionByDefault) {
        this.applyServerSideEncryptionByDefault = applyServerSideEncryptionByDefault;
    }

    public Boolean bucketKeyEnabled() {
        return bucketKeyEnabled;
    }

    public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
        this.bucketKeyEnabled = bucketKeyEnabled;
    }

    static final class Builder {
        private ServerSideEncryptionByDefault applyServerSideEncryptionByDefault;

        private Boolean bucketKeyEnabled;

        private Builder() {
        }

        private Builder(ServerSideEncryptionRule model) {
            applyServerSideEncryptionByDefault(model.applyServerSideEncryptionByDefault);
            bucketKeyEnabled(model.bucketKeyEnabled);
        }

        public ServerSideEncryptionRule build() {
            return new com.amazonaws.s3.model.ServerSideEncryptionRule(this);
        }

        /**
         * <p>Specifies the default server-side encryption to apply to new objects in the bucket. If a
         *          PUT Object request doesn't specify any server-side encryption, this default encryption will
         *          be applied.</p>
         */
        public final Builder applyServerSideEncryptionByDefault(
                ServerSideEncryptionByDefault applyServerSideEncryptionByDefault) {
            this.applyServerSideEncryptionByDefault = applyServerSideEncryptionByDefault;
            return this;
        }

        /**
         * <p>Specifies whether Amazon S3 should use an S3 Bucket Key with server-side encryption using KMS (SSE-KMS) for new objects in the bucket. Existing objects are not affected. Setting the <code>BucketKeyEnabled</code> element to <code>true</code> causes Amazon S3 to use an S3 Bucket Key. By default, S3 Bucket Key is not enabled.</p>
         *          <p>For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/bucket-key.html">Amazon S3 Bucket Keys</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder bucketKeyEnabled(Boolean bucketKeyEnabled) {
            this.bucketKeyEnabled = bucketKeyEnabled;
            return this;
        }
    }
}
