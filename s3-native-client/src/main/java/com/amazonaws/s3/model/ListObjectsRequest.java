// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListObjectsRequest {
    private String bucket;

    private String delimiter;

    private EncodingType encodingType;

    private String marker;

    private Integer maxKeys;

    private String prefix;

    private RequestPayer requestPayer;

    private String expectedBucketOwner;

    private ListObjectsRequest() {
        this.bucket = null;
        this.delimiter = null;
        this.encodingType = null;
        this.marker = null;
        this.maxKeys = null;
        this.prefix = null;
        this.requestPayer = null;
        this.expectedBucketOwner = null;
    }

    private ListObjectsRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.delimiter = builder.delimiter;
        this.encodingType = builder.encodingType;
        this.marker = builder.marker;
        this.maxKeys = builder.maxKeys;
        this.prefix = builder.prefix;
        this.requestPayer = builder.requestPayer;
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

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String delimiter() {
        return delimiter;
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    public EncodingType encodingType() {
        return encodingType;
    }

    public void setEncodingType(final EncodingType encodingType) {
        this.encodingType = encodingType;
    }

    public String marker() {
        return marker;
    }

    public void setMarker(final String marker) {
        this.marker = marker;
    }

    public Integer maxKeys() {
        return maxKeys;
    }

    public void setMaxKeys(final Integer maxKeys) {
        this.maxKeys = maxKeys;
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String delimiter;

        private EncodingType encodingType;

        private String marker;

        private Integer maxKeys;

        private String prefix;

        private RequestPayer requestPayer;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(ListObjectsRequest model) {
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
            return new com.amazonaws.s3.model.ListObjectsRequest(this);
        }

        /**
         * <p>The name of the bucket containing the objects.</p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>A delimiter is a character you use to group keys.</p>
         */
        public final Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public final Builder encodingType(EncodingType encodingType) {
            this.encodingType = encodingType;
            return this;
        }

        /**
         * <p>Specifies the key to start with when listing objects in a bucket.</p>
         */
        public final Builder marker(String marker) {
            this.marker = marker;
            return this;
        }

        /**
         * <p>Sets the maximum number of keys returned in the response. By default the API returns up
         *          to 1,000 key names. The response might contain fewer keys but will never contain more.
         *       </p>
         */
        public final Builder maxKeys(Integer maxKeys) {
            this.maxKeys = maxKeys;
            return this;
        }

        /**
         * <p>Limits the response to keys that begin with the specified prefix.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>Confirms that the requester knows that she or he will be charged for the list objects
         *          request. Bucket owners need not specify this parameter in their requests.</p>
         */
        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
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
