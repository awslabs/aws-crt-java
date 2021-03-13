// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectLockConfigurationRequest {
    /**
     * <p>The bucket whose Object Lock configuration you want to create or replace.</p>
     */
    String bucket;

    /**
     * <p>The Object Lock configuration that you want to apply to the specified bucket.</p>
     */
    ObjectLockConfiguration objectLockConfiguration;

    RequestPayer requestPayer;

    /**
     * <p>A token to allow Object Lock to be enabled for an existing bucket.</p>
     */
    String token;

    /**
     * <p>The MD5 hash for the request body.</p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    PutObjectLockConfigurationRequest() {
        this.bucket = "";
        this.objectLockConfiguration = null;
        this.requestPayer = null;
        this.token = "";
        this.contentMD5 = "";
        this.expectedBucketOwner = "";
    }

    protected PutObjectLockConfigurationRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.objectLockConfiguration = builder.objectLockConfiguration;
        this.requestPayer = builder.requestPayer;
        this.token = builder.token;
        this.contentMD5 = builder.contentMD5;
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
        return Objects.hash(PutObjectLockConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutObjectLockConfigurationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public ObjectLockConfiguration objectLockConfiguration() {
        return objectLockConfiguration;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public String token() {
        return token;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder objectLockConfiguration(ObjectLockConfiguration objectLockConfiguration);

        Builder requestPayer(RequestPayer requestPayer);

        Builder token(String token);

        Builder contentMD5(String contentMD5);

        Builder expectedBucketOwner(String expectedBucketOwner);

        PutObjectLockConfigurationRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket whose Object Lock configuration you want to create or replace.</p>
         */
        String bucket;

        /**
         * <p>The Object Lock configuration that you want to apply to the specified bucket.</p>
         */
        ObjectLockConfiguration objectLockConfiguration;

        RequestPayer requestPayer;

        /**
         * <p>A token to allow Object Lock to be enabled for an existing bucket.</p>
         */
        String token;

        /**
         * <p>The MD5 hash for the request body.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutObjectLockConfigurationRequest model) {
            bucket(model.bucket);
            objectLockConfiguration(model.objectLockConfiguration);
            requestPayer(model.requestPayer);
            token(model.token);
            contentMD5(model.contentMD5);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutObjectLockConfigurationRequest build() {
            return new PutObjectLockConfigurationRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder objectLockConfiguration(
                ObjectLockConfiguration objectLockConfiguration) {
            this.objectLockConfiguration = objectLockConfiguration;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
            return this;
        }

        public final Builder token(String token) {
            this.token = token;
            return this;
        }

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
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

        public ObjectLockConfiguration objectLockConfiguration() {
            return objectLockConfiguration;
        }

        public RequestPayer requestPayer() {
            return requestPayer;
        }

        public String token() {
            return token;
        }

        public String contentMD5() {
            return contentMD5;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }
    }
}
