// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListObjectsV2Request {
    private String bucket;

    private String delimiter;

    private EncodingType encodingType;

    private Integer maxKeys;

    private String prefix;

    private String continuationToken;

    private Boolean fetchOwner;

    private String startAfter;

    private RequestPayer requestPayer;

    private String expectedBucketOwner;

    private ListObjectsV2Request() {
        this.bucket = null;
        this.delimiter = null;
        this.encodingType = null;
        this.maxKeys = null;
        this.prefix = null;
        this.continuationToken = null;
        this.fetchOwner = null;
        this.startAfter = null;
        this.requestPayer = null;
        this.expectedBucketOwner = null;
    }

    private ListObjectsV2Request(Builder builder) {
        this.bucket = builder.bucket;
        this.delimiter = builder.delimiter;
        this.encodingType = builder.encodingType;
        this.maxKeys = builder.maxKeys;
        this.prefix = builder.prefix;
        this.continuationToken = builder.continuationToken;
        this.fetchOwner = builder.fetchOwner;
        this.startAfter = builder.startAfter;
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
        return Objects.hash(ListObjectsV2Request.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListObjectsV2Request);
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

    public String continuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(final String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public Boolean fetchOwner() {
        return fetchOwner;
    }

    public void setFetchOwner(final Boolean fetchOwner) {
        this.fetchOwner = fetchOwner;
    }

    public String startAfter() {
        return startAfter;
    }

    public void setStartAfter(final String startAfter) {
        this.startAfter = startAfter;
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

        private Integer maxKeys;

        private String prefix;

        private String continuationToken;

        private Boolean fetchOwner;

        private String startAfter;

        private RequestPayer requestPayer;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(ListObjectsV2Request model) {
            bucket(model.bucket);
            delimiter(model.delimiter);
            encodingType(model.encodingType);
            maxKeys(model.maxKeys);
            prefix(model.prefix);
            continuationToken(model.continuationToken);
            fetchOwner(model.fetchOwner);
            startAfter(model.startAfter);
            requestPayer(model.requestPayer);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public ListObjectsV2Request build() {
            return new com.amazonaws.s3.model.ListObjectsV2Request(this);
        }

        /**
         * <p>Bucket name to list. </p>
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

        /**
         * <p>Encoding type used by Amazon S3 to encode object keys in the response.</p>
         */
        public final Builder encodingType(EncodingType encodingType) {
            this.encodingType = encodingType;
            return this;
        }

        /**
         * <p>Sets the maximum number of keys returned in the response. By default the API returns up
         *          to 1,000 key names. The response might contain fewer keys but will never contain
         *          more.</p>
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
         * <p>ContinuationToken indicates Amazon S3 that the list is being continued on this bucket with a
         *          token. ContinuationToken is obfuscated and is not a real key.</p>
         */
        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
            return this;
        }

        /**
         * <p>The owner field is not present in listV2 by default, if you want to return owner field
         *          with each key in the result then set the fetch owner field to true.</p>
         */
        public final Builder fetchOwner(Boolean fetchOwner) {
            this.fetchOwner = fetchOwner;
            return this;
        }

        /**
         * <p>StartAfter is where you want Amazon S3 to start listing from. Amazon S3 starts listing after this
         *          specified key. StartAfter can be any key in the bucket.</p>
         */
        public final Builder startAfter(String startAfter) {
            this.startAfter = startAfter;
            return this;
        }

        /**
         * <p>Confirms that the requester knows that she or he will be charged for the list objects
         *          request in V2 style. Bucket owners need not specify this parameter in their
         *          requests.</p>
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
