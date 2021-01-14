// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListObjectsV2Request {
    /**
     * <p>Bucket name to list. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>A delimiter is a character you use to group keys.</p>
     */
    String delimiter;

    /**
     * <p>Encoding type used by Amazon S3 to encode object keys in the response.</p>
     */
    EncodingType encodingType;

    /**
     * <p>Sets the maximum number of keys returned in the response. By default the API returns up
     *          to 1,000 key names. The response might contain fewer keys but will never contain
     *          more.</p>
     */
    Integer maxKeys;

    /**
     * <p>Limits the response to keys that begin with the specified prefix.</p>
     */
    String prefix;

    /**
     * <p>ContinuationToken indicates Amazon S3 that the list is being continued on this bucket with a
     *          token. ContinuationToken is obfuscated and is not a real key.</p>
     */
    String continuationToken;

    /**
     * <p>The owner field is not present in listV2 by default, if you want to return owner field
     *          with each key in the result then set the fetch owner field to true.</p>
     */
    Boolean fetchOwner;

    /**
     * <p>StartAfter is where you want Amazon S3 to start listing from. Amazon S3 starts listing after this
     *          specified key. StartAfter can be any key in the bucket.</p>
     */
    String startAfter;

    /**
     * <p>Confirms that the requester knows that she or he will be charged for the list objects
     *          request in V2 style. Bucket owners need not specify this parameter in their
     *          requests.</p>
     */
    RequestPayer requestPayer;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    ListObjectsV2Request() {
        this.bucket = "";
        this.delimiter = "";
        this.encodingType = null;
        this.maxKeys = null;
        this.prefix = "";
        this.continuationToken = "";
        this.fetchOwner = null;
        this.startAfter = "";
        this.requestPayer = null;
        this.expectedBucketOwner = "";
    }

    protected ListObjectsV2Request(BuilderImpl builder) {
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

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String delimiter() {
        return delimiter;
    }

    public EncodingType encodingType() {
        return encodingType;
    }

    public Integer maxKeys() {
        return maxKeys;
    }

    public String prefix() {
        return prefix;
    }

    public String continuationToken() {
        return continuationToken;
    }

    public Boolean fetchOwner() {
        return fetchOwner;
    }

    public String startAfter() {
        return startAfter;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder delimiter(String delimiter);

        Builder encodingType(EncodingType encodingType);

        Builder maxKeys(Integer maxKeys);

        Builder prefix(String prefix);

        Builder continuationToken(String continuationToken);

        Builder fetchOwner(Boolean fetchOwner);

        Builder startAfter(String startAfter);

        Builder requestPayer(RequestPayer requestPayer);

        Builder expectedBucketOwner(String expectedBucketOwner);

        ListObjectsV2Request build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Bucket name to list. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>A delimiter is a character you use to group keys.</p>
         */
        String delimiter;

        /**
         * <p>Encoding type used by Amazon S3 to encode object keys in the response.</p>
         */
        EncodingType encodingType;

        /**
         * <p>Sets the maximum number of keys returned in the response. By default the API returns up
         *          to 1,000 key names. The response might contain fewer keys but will never contain
         *          more.</p>
         */
        Integer maxKeys;

        /**
         * <p>Limits the response to keys that begin with the specified prefix.</p>
         */
        String prefix;

        /**
         * <p>ContinuationToken indicates Amazon S3 that the list is being continued on this bucket with a
         *          token. ContinuationToken is obfuscated and is not a real key.</p>
         */
        String continuationToken;

        /**
         * <p>The owner field is not present in listV2 by default, if you want to return owner field
         *          with each key in the result then set the fetch owner field to true.</p>
         */
        Boolean fetchOwner;

        /**
         * <p>StartAfter is where you want Amazon S3 to start listing from. Amazon S3 starts listing after this
         *          specified key. StartAfter can be any key in the bucket.</p>
         */
        String startAfter;

        /**
         * <p>Confirms that the requester knows that she or he will be charged for the list objects
         *          request in V2 style. Bucket owners need not specify this parameter in their
         *          requests.</p>
         */
        RequestPayer requestPayer;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListObjectsV2Request model) {
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
            return new ListObjectsV2Request(this);
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

        public final Builder maxKeys(Integer maxKeys) {
            this.maxKeys = maxKeys;
            return this;
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
            return this;
        }

        public final Builder fetchOwner(Boolean fetchOwner) {
            this.fetchOwner = fetchOwner;
            return this;
        }

        public final Builder startAfter(String startAfter) {
            this.startAfter = startAfter;
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

        public Integer maxKeys() {
            return maxKeys;
        }

        public String prefix() {
            return prefix;
        }

        public String continuationToken() {
            return continuationToken;
        }

        public Boolean fetchOwner() {
            return fetchOwner;
        }

        public String startAfter() {
            return startAfter;
        }

        public RequestPayer requestPayer() {
            return requestPayer;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }
    }
}
