// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListBucketIntelligentTieringConfigurationsRequest {
    private String bucket;

    private String continuationToken;

    private ListBucketIntelligentTieringConfigurationsRequest() {
        this.bucket = null;
        this.continuationToken = null;
    }

    private ListBucketIntelligentTieringConfigurationsRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.continuationToken = builder.continuationToken;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ListBucketIntelligentTieringConfigurationsRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListBucketIntelligentTieringConfigurationsRequest);
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

    static final class Builder {
        private String bucket;

        private String continuationToken;

        private Builder() {
        }

        private Builder(ListBucketIntelligentTieringConfigurationsRequest model) {
            bucket(model.bucket);
            continuationToken(model.continuationToken);
        }

        public ListBucketIntelligentTieringConfigurationsRequest build() {
            return new com.amazonaws.s3.model.ListBucketIntelligentTieringConfigurationsRequest(this);
        }

        /**
         * <p>The name of the Amazon S3 bucket whose configuration you want to modify or retrieve.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The ContinuationToken that represents a placeholder from where this request should
         *          begin.</p>
         */
        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
            return this;
        }
    }
}
