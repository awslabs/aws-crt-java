// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;
import software.amazon.awssdk.crt.http.HttpHeader;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketPolicyRequest {
    /**
     * <p>The name of the bucket.</p>
     */
    String bucket;

    /**
     * <p>The MD5 hash of the request body.</p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>Set this parameter to true to confirm that you want to remove your permissions to change
     *          this bucket policy in the future.</p>
     */
    Boolean confirmRemoveSelfBucketAccess;

    /**
     * <p>The bucket policy as a JSON document.</p>
     */
    String policy;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    HttpHeader[] customHeaders;

    String customQueryParameters;

    PutBucketPolicyRequest() {
        this.bucket = "";
        this.contentMD5 = "";
        this.confirmRemoveSelfBucketAccess = null;
        this.policy = "";
        this.expectedBucketOwner = "";
        this.customHeaders = null;
        this.customQueryParameters = "";
    }

    protected PutBucketPolicyRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.confirmRemoveSelfBucketAccess = builder.confirmRemoveSelfBucketAccess;
        this.policy = builder.policy;
        this.expectedBucketOwner = builder.expectedBucketOwner;
        this.customHeaders = builder.customHeaders;
        this.customQueryParameters = builder.customQueryParameters;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String contentMD5() {
        return contentMD5;
    }

    public Boolean confirmRemoveSelfBucketAccess() {
        return confirmRemoveSelfBucketAccess;
    }

    public String policy() {
        return policy;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public HttpHeader[] customHeaders() {
        return customHeaders;
    }

    public String customQueryParameters() {
        return customQueryParameters;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder contentMD5(String contentMD5);

        Builder confirmRemoveSelfBucketAccess(Boolean confirmRemoveSelfBucketAccess);

        Builder policy(String policy);

        Builder expectedBucketOwner(String expectedBucketOwner);

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(String customQueryParameters);

        PutBucketPolicyRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket.</p>
         */
        String bucket;

        /**
         * <p>The MD5 hash of the request body.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>Set this parameter to true to confirm that you want to remove your permissions to change
         *          this bucket policy in the future.</p>
         */
        Boolean confirmRemoveSelfBucketAccess;

        /**
         * <p>The bucket policy as a JSON document.</p>
         */
        String policy;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        HttpHeader[] customHeaders;

        String customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketPolicyRequest model) {
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            confirmRemoveSelfBucketAccess(model.confirmRemoveSelfBucketAccess);
            policy(model.policy);
            expectedBucketOwner(model.expectedBucketOwner);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public PutBucketPolicyRequest build() {
            return new PutBucketPolicyRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public final Builder confirmRemoveSelfBucketAccess(Boolean confirmRemoveSelfBucketAccess) {
            this.confirmRemoveSelfBucketAccess = confirmRemoveSelfBucketAccess;
            return this;
        }

        public final Builder policy(String policy) {
            this.policy = policy;
            return this;
        }

        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }

        public final Builder customHeaders(HttpHeader[] customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        public final Builder customQueryParameters(String customQueryParameters) {
            this.customQueryParameters = customQueryParameters;
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

        public Boolean confirmRemoveSelfBucketAccess() {
            return confirmRemoveSelfBucketAccess;
        }

        public String policy() {
            return policy;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public HttpHeader[] customHeaders() {
            return customHeaders;
        }

        public String customQueryParameters() {
            return customQueryParameters;
        }
    }
}
