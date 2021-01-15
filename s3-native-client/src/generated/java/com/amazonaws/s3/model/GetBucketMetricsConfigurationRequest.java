// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketMetricsConfigurationRequest {
    /**
     * <p>The name of the bucket containing the metrics configuration to retrieve.</p>
     */
    String bucket;

    /**
     * <p>The ID used to identify the metrics configuration.</p>
     */
    String id;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    GetBucketMetricsConfigurationRequest() {
        this.bucket = "";
        this.id = "";
        this.expectedBucketOwner = "";
    }

    protected GetBucketMetricsConfigurationRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.id = builder.id;
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
        return Objects.hash(GetBucketMetricsConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketMetricsConfigurationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String id() {
        return id;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder id(String id);

        Builder expectedBucketOwner(String expectedBucketOwner);

        GetBucketMetricsConfigurationRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket containing the metrics configuration to retrieve.</p>
         */
        String bucket;

        /**
         * <p>The ID used to identify the metrics configuration.</p>
         */
        String id;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketMetricsConfigurationRequest model) {
            bucket(model.bucket);
            id(model.id);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public GetBucketMetricsConfigurationRequest build() {
            return new GetBucketMetricsConfigurationRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder id(String id) {
            this.id = id;
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

        public String id() {
            return id;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }
    }
}
