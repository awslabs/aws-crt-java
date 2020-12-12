// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListBucketInventoryConfigurationsRequest {
    private String bucket;

    private String continuationToken;

    private String expectedBucketOwner;

    private ListBucketInventoryConfigurationsRequest() {
        this.bucket = null;
        this.continuationToken = null;
        this.expectedBucketOwner = null;
    }

    private ListBucketInventoryConfigurationsRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.continuationToken = builder.continuationToken;
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
        return Objects.hash(ListBucketInventoryConfigurationsRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListBucketInventoryConfigurationsRequest);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String continuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(final String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String continuationToken;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(ListBucketInventoryConfigurationsRequest model) {
            bucket(model.bucket);
            continuationToken(model.continuationToken);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public ListBucketInventoryConfigurationsRequest build() {
            return new com.amazonaws.s3.model.ListBucketInventoryConfigurationsRequest(this);
        }

        /**
         * <p>The name of the bucket containing the inventory configurations to retrieve.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The marker used to continue an inventory configuration listing that has been truncated.
         *          Use the NextContinuationToken from a previously truncated list response to continue the
         *          listing. The continuation token is an opaque value that Amazon S3 understands.</p>
         */
        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
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
