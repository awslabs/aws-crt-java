// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketPolicyRequest {
    private String bucket;

    private String contentMD5;

    private Boolean confirmRemoveSelfBucketAccess;

    private String policy;

    private String expectedBucketOwner;

    private PutBucketPolicyRequest() {
        this.bucket = null;
        this.contentMD5 = null;
        this.confirmRemoveSelfBucketAccess = null;
        this.policy = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketPolicyRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.confirmRemoveSelfBucketAccess = builder.confirmRemoveSelfBucketAccess;
        this.policy = builder.policy;
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
        return Objects.hash(PutBucketPolicyRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketPolicyRequest);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public Boolean confirmRemoveSelfBucketAccess() {
        return confirmRemoveSelfBucketAccess;
    }

    public void setConfirmRemoveSelfBucketAccess(final Boolean confirmRemoveSelfBucketAccess) {
        this.confirmRemoveSelfBucketAccess = confirmRemoveSelfBucketAccess;
    }

    public String policy() {
        return policy;
    }

    public void setPolicy(final String policy) {
        this.policy = policy;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String contentMD5;

        private Boolean confirmRemoveSelfBucketAccess;

        private String policy;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketPolicyRequest model) {
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            confirmRemoveSelfBucketAccess(model.confirmRemoveSelfBucketAccess);
            policy(model.policy);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketPolicyRequest build() {
            return new com.amazonaws.s3.model.PutBucketPolicyRequest(this);
        }

        /**
         * <p>The name of the bucket.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The MD5 hash of the request body.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        /**
         * <p>Set this parameter to true to confirm that you want to remove your permissions to change
         *          this bucket policy in the future.</p>
         */
        public final Builder confirmRemoveSelfBucketAccess(Boolean confirmRemoveSelfBucketAccess) {
            this.confirmRemoveSelfBucketAccess = confirmRemoveSelfBucketAccess;
            return this;
        }

        /**
         * <p>The bucket policy as a JSON document.</p>
         */
        public final Builder policy(String policy) {
            this.policy = policy;
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
