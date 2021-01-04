// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectTorrentRequest {
    /**
     * <p>The name of the bucket containing the object for which to get the torrent files.</p>
     */
    String bucket;

    /**
     * <p>The object key for which to get the information.</p>
     */
    String key;

    RequestPayer requestPayer;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    GetObjectTorrentRequest() {
        this.bucket = "";
        this.key = "";
        this.requestPayer = null;
        this.expectedBucketOwner = "";
    }

    protected GetObjectTorrentRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.requestPayer = builder.requestPayer;
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
        return Objects.hash(GetObjectTorrentRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectTorrentRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String key() {
        return key;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder key(String key);

        Builder requestPayer(RequestPayer requestPayer);

        Builder expectedBucketOwner(String expectedBucketOwner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket containing the object for which to get the torrent files.</p>
         */
        String bucket;

        /**
         * <p>The object key for which to get the information.</p>
         */
        String key;

        RequestPayer requestPayer;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetObjectTorrentRequest model) {
            bucket(model.bucket);
            key(model.key);
            requestPayer(model.requestPayer);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public GetObjectTorrentRequest build() {
            return new GetObjectTorrentRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
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

        public String key() {
            return key;
        }

        public RequestPayer requestPayer() {
            return requestPayer;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setRequestPayer(final RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
