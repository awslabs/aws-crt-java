// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketIntelligentTieringConfigurationRequest {
    private String bucket;

    private String id;

    private GetBucketIntelligentTieringConfigurationRequest() {
        this.bucket = null;
        this.id = null;
    }

    private GetBucketIntelligentTieringConfigurationRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.id = builder.id;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketIntelligentTieringConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketIntelligentTieringConfigurationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String id() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    static final class Builder {
        private String bucket;

        private String id;

        private Builder() {
        }

        private Builder(GetBucketIntelligentTieringConfigurationRequest model) {
            bucket(model.bucket);
            id(model.id);
        }

        public GetBucketIntelligentTieringConfigurationRequest build() {
            return new com.amazonaws.s3.model.GetBucketIntelligentTieringConfigurationRequest(this);
        }

        /**
         * <p>The name of the Amazon S3 bucket whose configuration you want to modify or retrieve.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The ID used to identify the S3 Intelligent-Tiering configuration.</p>
         */
        public final Builder id(String id) {
            this.id = id;
            return this;
        }
    }
}
