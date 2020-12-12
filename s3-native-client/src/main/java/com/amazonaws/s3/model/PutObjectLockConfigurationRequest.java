// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectLockConfigurationRequest {
    private String bucket;

    private ObjectLockConfiguration objectLockConfiguration;

    private RequestPayer requestPayer;

    private String token;

    private String contentMD5;

    private String expectedBucketOwner;

    private PutObjectLockConfigurationRequest() {
        this.bucket = null;
        this.objectLockConfiguration = null;
        this.requestPayer = null;
        this.token = null;
        this.contentMD5 = null;
        this.expectedBucketOwner = null;
    }

    private PutObjectLockConfigurationRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.objectLockConfiguration = builder.objectLockConfiguration;
        this.requestPayer = builder.requestPayer;
        this.token = builder.token;
        this.contentMD5 = builder.contentMD5;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public ObjectLockConfiguration objectLockConfiguration() {
        return objectLockConfiguration;
    }

    public void setObjectLockConfiguration(final ObjectLockConfiguration objectLockConfiguration) {
        this.objectLockConfiguration = objectLockConfiguration;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public String token() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private ObjectLockConfiguration objectLockConfiguration;

        private RequestPayer requestPayer;

        private String token;

        private String contentMD5;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutObjectLockConfigurationRequest model) {
            bucket(model.bucket);
            objectLockConfiguration(model.objectLockConfiguration);
            requestPayer(model.requestPayer);
            token(model.token);
            contentMD5(model.contentMD5);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutObjectLockConfigurationRequest build() {
            return new com.amazonaws.s3.model.PutObjectLockConfigurationRequest(this);
        }

        /**
         * <p>The bucket whose Object Lock configuration you want to create or replace.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The Object Lock configuration that you want to apply to the specified bucket.</p>
         */
        public final Builder objectLockConfiguration(
                ObjectLockConfiguration objectLockConfiguration) {
            this.objectLockConfiguration = objectLockConfiguration;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
            return this;
        }

        /**
         * <p>A token to allow Object Lock to be enabled for an existing bucket.</p>
         */
        public final Builder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * <p>The MD5 hash for the request body.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }
    }
}
