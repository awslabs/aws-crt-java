// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListObjectsRequest {
    /**
     * <p>The name of the bucket containing the objects.</p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>A delimiter is a character you use to group keys.</p>
     */
    String delimiter;

    EncodingType encodingType;

    /**
     * <p>Specifies the key to start with when listing objects in a bucket.</p>
     */
    String marker;

    /**
     * <p>Sets the maximum number of keys returned in the response. By default the API returns up
     *          to 1,000 key names. The response might contain fewer keys but will never contain more.
     *       </p>
     */
    Integer maxKeys;

    /**
     * <p>Limits the response to keys that begin with the specified prefix.</p>
     */
    String prefix;

    /**
     * <p>Confirms that the requester knows that she or he will be charged for the list objects
     *          request. Bucket owners need not specify this parameter in their requests.</p>
     */
    RequestPayer requestPayer;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    ListObjectsRequest() {
        this.bucket = "";
        this.delimiter = "";
        this.encodingType = null;
        this.marker = "";
        this.maxKeys = null;
        this.prefix = "";
        this.requestPayer = null;
        this.expectedBucketOwner = "";
    }

    protected ListObjectsRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.delimiter = builder.delimiter;
        this.encodingType = builder.encodingType;
        this.marker = builder.marker;
        this.maxKeys = builder.maxKeys;
        this.prefix = builder.prefix;
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
        return Objects.hash(ListObjectsRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListObjectsRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String delimiter() {
        return delimiter;
    }

    public EncodingType encodingType() {
        return encodingType;
    }

    public String marker() {
        return marker;
    }

    public Integer maxKeys() {
        return maxKeys;
    }

    public String prefix() {
        return prefix;
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

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    public void setEncodingType(final EncodingType encodingType) {
        this.encodingType = encodingType;
    }

    public void setMarker(final String marker) {
        this.marker = marker;
    }

    public void setMaxKeys(final Integer maxKeys) {
        this.maxKeys = maxKeys;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder delimiter(String delimiter);

        Builder encodingType(EncodingType encodingType);

        Builder marker(String marker);

        Builder maxKeys(Integer maxKeys);

        Builder prefix(String prefix);

        Builder requestPayer(RequestPayer requestPayer);

        Builder expectedBucketOwner(String expectedBucketOwner);

        ListObjectsRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket containing the objects.</p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>A delimiter is a character you use to group keys.</p>
         */
        String delimiter;

        EncodingType encodingType;

        /**
         * <p>Specifies the key to start with when listing objects in a bucket.</p>
         */
        String marker;

        /**
         * <p>Sets the maximum number of keys returned in the response. By default the API returns up
         *          to 1,000 key names. The response might contain fewer keys but will never contain more.
         *       </p>
         */
        Integer maxKeys;

        /**
         * <p>Limits the response to keys that begin with the specified prefix.</p>
         */
        String prefix;

        /**
         * <p>Confirms that the requester knows that she or he will be charged for the list objects
         *          request. Bucket owners need not specify this parameter in their requests.</p>
         */
        RequestPayer requestPayer;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListObjectsRequest model) {
            bucket(model.bucket);
            delimiter(model.delimiter);
            encodingType(model.encodingType);
            marker(model.marker);
            maxKeys(model.maxKeys);
            prefix(model.prefix);
            requestPayer(model.requestPayer);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public ListObjectsRequest build() {
            return new ListObjectsRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public final Builder encodingType(EncodingType encodingType) {
            this.encodingType = encodingType;
            return this;
        }

        public final Builder marker(String marker) {
            this.marker = marker;
            return this;
        }

        public final Builder maxKeys(Integer maxKeys) {
            this.maxKeys = maxKeys;
            return this;
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
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

        public String delimiter() {
            return delimiter;
        }

        public EncodingType encodingType() {
            return encodingType;
        }

        public String marker() {
            return marker;
        }

        public Integer maxKeys() {
            return maxKeys;
        }

        public String prefix() {
            return prefix;
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

        public void setDelimiter(final String delimiter) {
            this.delimiter = delimiter;
        }

        public void setEncodingType(final EncodingType encodingType) {
            this.encodingType = encodingType;
        }

        public void setMarker(final String marker) {
            this.marker = marker;
        }

        public void setMaxKeys(final Integer maxKeys) {
            this.maxKeys = maxKeys;
        }

        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }

        public void setRequestPayer(final RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
