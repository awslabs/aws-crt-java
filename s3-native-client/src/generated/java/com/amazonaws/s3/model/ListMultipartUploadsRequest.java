// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;
import software.amazon.awssdk.crt.http.HttpHeader;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListMultipartUploadsRequest {
    /**
     * <p>The name of the bucket to which the multipart upload was initiated. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>Character you use to group keys.</p>
     *          <p>All keys that contain the same string between the prefix, if specified, and the first
     *          occurrence of the delimiter after the prefix are grouped under a single result element,
     *             <code>CommonPrefixes</code>. If you don't specify the prefix parameter, then the
     *          substring starts at the beginning of the key. The keys that are grouped under
     *             <code>CommonPrefixes</code> result element are not returned elsewhere in the
     *          response.</p>
     */
    String delimiter;

    EncodingType encodingType;

    /**
     * <p>Together with upload-id-marker, this parameter specifies the multipart upload after
     *          which listing should begin.</p>
     *          <p>If <code>upload-id-marker</code> is not specified, only the keys lexicographically
     *          greater than the specified <code>key-marker</code> will be included in the list.</p>
     *
     *          <p>If <code>upload-id-marker</code> is specified, any multipart uploads for a key equal to
     *          the <code>key-marker</code> might also be included, provided those multipart uploads have
     *          upload IDs lexicographically greater than the specified
     *          <code>upload-id-marker</code>.</p>
     */
    String keyMarker;

    /**
     * <p>Sets the maximum number of multipart uploads, from 1 to 1,000, to return in the response
     *          body. 1,000 is the maximum number of uploads that can be returned in a response.</p>
     */
    Integer maxUploads;

    /**
     * <p>Lists in-progress uploads only for those keys that begin with the specified prefix. You
     *          can use prefixes to separate a bucket into different grouping of keys. (You can think of
     *          using prefix to make groups in the same way you'd use a folder in a file system.)</p>
     */
    String prefix;

    /**
     * <p>Together with key-marker, specifies the multipart upload after which listing should
     *          begin. If key-marker is not specified, the upload-id-marker parameter is ignored.
     *          Otherwise, any multipart uploads for a key equal to the key-marker might be included in the
     *          list only if they have an upload ID lexicographically greater than the specified
     *             <code>upload-id-marker</code>.</p>
     */
    String uploadIdMarker;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    HttpHeader[] customHeaders;

    Map<String, String> customQueryParameters;

    ListMultipartUploadsRequest() {
        this.bucket = "";
        this.delimiter = "";
        this.encodingType = null;
        this.keyMarker = "";
        this.maxUploads = null;
        this.prefix = "";
        this.uploadIdMarker = "";
        this.expectedBucketOwner = "";
        this.customHeaders = null;
        this.customQueryParameters = null;
    }

    protected ListMultipartUploadsRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.delimiter = builder.delimiter;
        this.encodingType = builder.encodingType;
        this.keyMarker = builder.keyMarker;
        this.maxUploads = builder.maxUploads;
        this.prefix = builder.prefix;
        this.uploadIdMarker = builder.uploadIdMarker;
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
        return Objects.hash(ListMultipartUploadsRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListMultipartUploadsRequest);
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

    public String keyMarker() {
        return keyMarker;
    }

    public Integer maxUploads() {
        return maxUploads;
    }

    public String prefix() {
        return prefix;
    }

    public String uploadIdMarker() {
        return uploadIdMarker;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public HttpHeader[] customHeaders() {
        return customHeaders;
    }

    public Map<String, String> customQueryParameters() {
        return customQueryParameters;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder delimiter(String delimiter);

        Builder encodingType(EncodingType encodingType);

        Builder keyMarker(String keyMarker);

        Builder maxUploads(Integer maxUploads);

        Builder prefix(String prefix);

        Builder uploadIdMarker(String uploadIdMarker);

        Builder expectedBucketOwner(String expectedBucketOwner);

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(Map<String, String> customQueryParameters);

        ListMultipartUploadsRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket to which the multipart upload was initiated. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>Character you use to group keys.</p>
         *          <p>All keys that contain the same string between the prefix, if specified, and the first
         *          occurrence of the delimiter after the prefix are grouped under a single result element,
         *             <code>CommonPrefixes</code>. If you don't specify the prefix parameter, then the
         *          substring starts at the beginning of the key. The keys that are grouped under
         *             <code>CommonPrefixes</code> result element are not returned elsewhere in the
         *          response.</p>
         */
        String delimiter;

        EncodingType encodingType;

        /**
         * <p>Together with upload-id-marker, this parameter specifies the multipart upload after
         *          which listing should begin.</p>
         *          <p>If <code>upload-id-marker</code> is not specified, only the keys lexicographically
         *          greater than the specified <code>key-marker</code> will be included in the list.</p>
         *
         *          <p>If <code>upload-id-marker</code> is specified, any multipart uploads for a key equal to
         *          the <code>key-marker</code> might also be included, provided those multipart uploads have
         *          upload IDs lexicographically greater than the specified
         *          <code>upload-id-marker</code>.</p>
         */
        String keyMarker;

        /**
         * <p>Sets the maximum number of multipart uploads, from 1 to 1,000, to return in the response
         *          body. 1,000 is the maximum number of uploads that can be returned in a response.</p>
         */
        Integer maxUploads;

        /**
         * <p>Lists in-progress uploads only for those keys that begin with the specified prefix. You
         *          can use prefixes to separate a bucket into different grouping of keys. (You can think of
         *          using prefix to make groups in the same way you'd use a folder in a file system.)</p>
         */
        String prefix;

        /**
         * <p>Together with key-marker, specifies the multipart upload after which listing should
         *          begin. If key-marker is not specified, the upload-id-marker parameter is ignored.
         *          Otherwise, any multipart uploads for a key equal to the key-marker might be included in the
         *          list only if they have an upload ID lexicographically greater than the specified
         *             <code>upload-id-marker</code>.</p>
         */
        String uploadIdMarker;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        HttpHeader[] customHeaders;

        Map<String, String> customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListMultipartUploadsRequest model) {
            bucket(model.bucket);
            delimiter(model.delimiter);
            encodingType(model.encodingType);
            keyMarker(model.keyMarker);
            maxUploads(model.maxUploads);
            prefix(model.prefix);
            uploadIdMarker(model.uploadIdMarker);
            expectedBucketOwner(model.expectedBucketOwner);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public ListMultipartUploadsRequest build() {
            return new ListMultipartUploadsRequest(this);
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

        public final Builder keyMarker(String keyMarker) {
            this.keyMarker = keyMarker;
            return this;
        }

        public final Builder maxUploads(Integer maxUploads) {
            this.maxUploads = maxUploads;
            return this;
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder uploadIdMarker(String uploadIdMarker) {
            this.uploadIdMarker = uploadIdMarker;
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

        public final Builder customQueryParameters(Map<String, String> customQueryParameters) {
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

        public String delimiter() {
            return delimiter;
        }

        public EncodingType encodingType() {
            return encodingType;
        }

        public String keyMarker() {
            return keyMarker;
        }

        public Integer maxUploads() {
            return maxUploads;
        }

        public String prefix() {
            return prefix;
        }

        public String uploadIdMarker() {
            return uploadIdMarker;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public HttpHeader[] customHeaders() {
            return customHeaders;
        }

        public Map<String, String> customQueryParameters() {
            return customQueryParameters;
        }
    }
}
