// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListMultipartUploadsOutput {
    private String bucket;

    private String keyMarker;

    private String uploadIdMarker;

    private String nextKeyMarker;

    private String prefix;

    private String delimiter;

    private String nextUploadIdMarker;

    private Integer maxUploads;

    private Boolean isTruncated;

    private List<MultipartUpload> uploads;

    private List<CommonPrefix> commonPrefixes;

    private EncodingType encodingType;

    private ListMultipartUploadsOutput() {
        this.bucket = null;
        this.keyMarker = null;
        this.uploadIdMarker = null;
        this.nextKeyMarker = null;
        this.prefix = null;
        this.delimiter = null;
        this.nextUploadIdMarker = null;
        this.maxUploads = null;
        this.isTruncated = null;
        this.uploads = null;
        this.commonPrefixes = null;
        this.encodingType = null;
    }

    private ListMultipartUploadsOutput(Builder builder) {
        this.bucket = builder.bucket;
        this.keyMarker = builder.keyMarker;
        this.uploadIdMarker = builder.uploadIdMarker;
        this.nextKeyMarker = builder.nextKeyMarker;
        this.prefix = builder.prefix;
        this.delimiter = builder.delimiter;
        this.nextUploadIdMarker = builder.nextUploadIdMarker;
        this.maxUploads = builder.maxUploads;
        this.isTruncated = builder.isTruncated;
        this.uploads = builder.uploads;
        this.commonPrefixes = builder.commonPrefixes;
        this.encodingType = builder.encodingType;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ListMultipartUploadsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListMultipartUploadsOutput);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String keyMarker() {
        return keyMarker;
    }

    public void setKeyMarker(final String keyMarker) {
        this.keyMarker = keyMarker;
    }

    public String uploadIdMarker() {
        return uploadIdMarker;
    }

    public void setUploadIdMarker(final String uploadIdMarker) {
        this.uploadIdMarker = uploadIdMarker;
    }

    public String nextKeyMarker() {
        return nextKeyMarker;
    }

    public void setNextKeyMarker(final String nextKeyMarker) {
        this.nextKeyMarker = nextKeyMarker;
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String delimiter() {
        return delimiter;
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    public String nextUploadIdMarker() {
        return nextUploadIdMarker;
    }

    public void setNextUploadIdMarker(final String nextUploadIdMarker) {
        this.nextUploadIdMarker = nextUploadIdMarker;
    }

    public Integer maxUploads() {
        return maxUploads;
    }

    public void setMaxUploads(final Integer maxUploads) {
        this.maxUploads = maxUploads;
    }

    public Boolean isTruncated() {
        return isTruncated;
    }

    public void setIsTruncated(final Boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public List<MultipartUpload> uploads() {
        return uploads;
    }

    public void setUploads(final List<MultipartUpload> uploads) {
        this.uploads = uploads;
    }

    public List<CommonPrefix> commonPrefixes() {
        return commonPrefixes;
    }

    public void setCommonPrefixes(final List<CommonPrefix> commonPrefixes) {
        this.commonPrefixes = commonPrefixes;
    }

    public EncodingType encodingType() {
        return encodingType;
    }

    public void setEncodingType(final EncodingType encodingType) {
        this.encodingType = encodingType;
    }

    static final class Builder {
        private String bucket;

        private String keyMarker;

        private String uploadIdMarker;

        private String nextKeyMarker;

        private String prefix;

        private String delimiter;

        private String nextUploadIdMarker;

        private Integer maxUploads;

        private Boolean isTruncated;

        private List<MultipartUpload> uploads;

        private List<CommonPrefix> commonPrefixes;

        private EncodingType encodingType;

        private Builder() {
        }

        private Builder(ListMultipartUploadsOutput model) {
            bucket(model.bucket);
            keyMarker(model.keyMarker);
            uploadIdMarker(model.uploadIdMarker);
            nextKeyMarker(model.nextKeyMarker);
            prefix(model.prefix);
            delimiter(model.delimiter);
            nextUploadIdMarker(model.nextUploadIdMarker);
            maxUploads(model.maxUploads);
            isTruncated(model.isTruncated);
            uploads(model.uploads);
            commonPrefixes(model.commonPrefixes);
            encodingType(model.encodingType);
        }

        public ListMultipartUploadsOutput build() {
            return new com.amazonaws.s3.model.ListMultipartUploadsOutput(this);
        }

        /**
         * <p>The name of the bucket to which the multipart upload was initiated.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The key at or after which the listing began.</p>
         */
        public final Builder keyMarker(String keyMarker) {
            this.keyMarker = keyMarker;
            return this;
        }

        /**
         * <p>Upload ID after which listing began.</p>
         */
        public final Builder uploadIdMarker(String uploadIdMarker) {
            this.uploadIdMarker = uploadIdMarker;
            return this;
        }

        /**
         * <p>When a list is truncated, this element specifies the value that should be used for the
         *          key-marker request parameter in a subsequent request.</p>
         */
        public final Builder nextKeyMarker(String nextKeyMarker) {
            this.nextKeyMarker = nextKeyMarker;
            return this;
        }

        /**
         * <p>When a prefix is provided in the request, this field contains the specified prefix. The
         *          result contains only keys starting with the specified prefix.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>Contains the delimiter you specified in the request. If you don't specify a delimiter in
         *          your request, this element is absent from the response.</p>
         */
        public final Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * <p>When a list is truncated, this element specifies the value that should be used for the
         *             <code>upload-id-marker</code> request parameter in a subsequent request.</p>
         */
        public final Builder nextUploadIdMarker(String nextUploadIdMarker) {
            this.nextUploadIdMarker = nextUploadIdMarker;
            return this;
        }

        /**
         * <p>Maximum number of multipart uploads that could have been included in the
         *          response.</p>
         */
        public final Builder maxUploads(Integer maxUploads) {
            this.maxUploads = maxUploads;
            return this;
        }

        /**
         * <p>Indicates whether the returned list of multipart uploads is truncated. A value of true
         *          indicates that the list was truncated. The list can be truncated if the number of multipart
         *          uploads exceeds the limit allowed or specified by max uploads.</p>
         */
        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        /**
         * <p>Container for elements related to a particular multipart upload. A response can contain
         *          zero or more <code>Upload</code> elements.</p>
         */
        public final Builder uploads(List<MultipartUpload> uploads) {
            this.uploads = uploads;
            return this;
        }

        /**
         * <p>If you specify a delimiter in the request, then the result returns each distinct key
         *          prefix containing the delimiter in a <code>CommonPrefixes</code> element. The distinct key
         *          prefixes are returned in the <code>Prefix</code> child element.</p>
         */
        public final Builder commonPrefixes(List<CommonPrefix> commonPrefixes) {
            this.commonPrefixes = commonPrefixes;
            return this;
        }

        /**
         * <p>Encoding type used by Amazon S3 to encode object keys in the response.</p>
         *          <p>If you specify <code>encoding-type</code> request parameter, Amazon S3 includes this element
         *          in the response, and returns encoded key name values in the following response
         *          elements:</p>
         *
         *          <p>
         *             <code>Delimiter</code>, <code>KeyMarker</code>, <code>Prefix</code>,
         *             <code>NextKeyMarker</code>, <code>Key</code>.</p>
         */
        public final Builder encodingType(EncodingType encodingType) {
            this.encodingType = encodingType;
            return this;
        }
    }
}
