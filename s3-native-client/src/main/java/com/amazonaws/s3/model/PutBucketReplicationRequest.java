// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketReplicationRequest {
    /**
     * <p>The name of the bucket</p>
     */
    String bucket;

    /**
     * <p>The base64-encoded 128-bit MD5 digest of the data. You must use this header as a message
     *          integrity check to verify that the request body was not corrupted in transit. For more
     *          information, see <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC 1864</a>.</p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    ReplicationConfiguration replicationConfiguration;

    /**
     * <p>A token to allow Object Lock to be enabled for an existing bucket.</p>
     */
    String token;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    PutBucketReplicationRequest() {
        this.bucket = "";
        this.contentMD5 = "";
        this.replicationConfiguration = null;
        this.token = "";
        this.expectedBucketOwner = "";
    }

    protected PutBucketReplicationRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.replicationConfiguration = builder.replicationConfiguration;
        this.token = builder.token;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(PutBucketReplicationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketReplicationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public ReplicationConfiguration replicationConfiguration() {
        return replicationConfiguration;
    }

    public String token() {
        return token;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public void setReplicationConfiguration(
            final ReplicationConfiguration replicationConfiguration) {
        this.replicationConfiguration = replicationConfiguration;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder contentMD5(String contentMD5);

        Builder replicationConfiguration(ReplicationConfiguration replicationConfiguration);

        Builder token(String token);

        Builder expectedBucketOwner(String expectedBucketOwner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket</p>
         */
        String bucket;

        /**
         * <p>The base64-encoded 128-bit MD5 digest of the data. You must use this header as a message
         *          integrity check to verify that the request body was not corrupted in transit. For more
         *          information, see <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC 1864</a>.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        ReplicationConfiguration replicationConfiguration;

        /**
         * <p>A token to allow Object Lock to be enabled for an existing bucket.</p>
         */
        String token;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketReplicationRequest model) {
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            replicationConfiguration(model.replicationConfiguration);
            token(model.token);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketReplicationRequest build() {
            return new PutBucketReplicationRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public final Builder replicationConfiguration(
                ReplicationConfiguration replicationConfiguration) {
            this.replicationConfiguration = replicationConfiguration;
            return this;
        }

        public final Builder token(String token) {
            this.token = token;
            return this;
        }

        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
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

        public String contentMD5() {
            return contentMD5;
        }

        public ReplicationConfiguration replicationConfiguration() {
            return replicationConfiguration;
        }

        public String token() {
            return token;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setContentMD5(final String contentMD5) {
            this.contentMD5 = contentMD5;
        }

        public void setReplicationConfiguration(
                final ReplicationConfiguration replicationConfiguration) {
            this.replicationConfiguration = replicationConfiguration;
        }

        public void setToken(final String token) {
            this.token = token;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
