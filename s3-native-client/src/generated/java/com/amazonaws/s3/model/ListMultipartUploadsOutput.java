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
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListMultipartUploadsOutput {
    /**
     * <p>The name of the bucket to which the multipart upload was initiated.</p>
     */
    String bucket;

    /**
     * <p>The key at or after which the listing began.</p>
     */
    String keyMarker;

    /**
     * <p>Upload ID after which listing began.</p>
     */
    String uploadIdMarker;

    /**
     * <p>When a list is truncated, this element specifies the value that should be used for the
     *          key-marker request parameter in a subsequent request.</p>
     */
    String nextKeyMarker;

    /**
     * <p>When a prefix is provided in the request, this field contains the specified prefix. The
     *          result contains only keys starting with the specified prefix.</p>
     */
    String prefix;

    /**
     * <p>Contains the delimiter you specified in the request. If you don't specify a delimiter in
     *          your request, this element is absent from the response.</p>
     */
    String delimiter;

    /**
     * <p>When a list is truncated, this element specifies the value that should be used for the
     *             <code>upload-id-marker</code> request parameter in a subsequent request.</p>
     */
    String nextUploadIdMarker;

    /**
     * <p>Maximum number of multipart uploads that could have been included in the
     *          response.</p>
     */
    Integer maxUploads;

    /**
     * <p>Indicates whether the returned list of multipart uploads is truncated. A value of true
     *          indicates that the list was truncated. The list can be truncated if the number of multipart
     *          uploads exceeds the limit allowed or specified by max uploads.</p>
     */
    Boolean isTruncated;

    /**
     * <p>Container for elements related to a particular multipart upload. A response can contain
     *          zero or more <code>Upload</code> elements.</p>
     */
    List<MultipartUpload> uploads;

    /**
     * <p>If you specify a delimiter in the request, then the result returns each distinct key
     *          prefix containing the delimiter in a <code>CommonPrefixes</code> element. The distinct key
     *          prefixes are returned in the <code>Prefix</code> child element.</p>
     */
    List<CommonPrefix> commonPrefixes;

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
    EncodingType encodingType;

    ListMultipartUploadsOutput() {
        this.bucket = "";
        this.keyMarker = "";
        this.uploadIdMarker = "";
        this.nextKeyMarker = "";
        this.prefix = "";
        this.delimiter = "";
        this.nextUploadIdMarker = "";
        this.maxUploads = null;
        this.isTruncated = null;
        this.uploads = null;
        this.commonPrefixes = null;
        this.encodingType = null;
    }

    protected ListMultipartUploadsOutput(BuilderImpl builder) {
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

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String keyMarker() {
        return keyMarker;
    }

    public String uploadIdMarker() {
        return uploadIdMarker;
    }

    public String nextKeyMarker() {
        return nextKeyMarker;
    }

    public String prefix() {
        return prefix;
    }

    public String delimiter() {
        return delimiter;
    }

    public String nextUploadIdMarker() {
        return nextUploadIdMarker;
    }

    public Integer maxUploads() {
        return maxUploads;
    }

    public Boolean isTruncated() {
        return isTruncated;
    }

    public List<MultipartUpload> uploads() {
        return uploads;
    }

    public List<CommonPrefix> commonPrefixes() {
        return commonPrefixes;
    }

    public EncodingType encodingType() {
        return encodingType;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder keyMarker(String keyMarker);

        Builder uploadIdMarker(String uploadIdMarker);

        Builder nextKeyMarker(String nextKeyMarker);

        Builder prefix(String prefix);

        Builder delimiter(String delimiter);

        Builder nextUploadIdMarker(String nextUploadIdMarker);

        Builder maxUploads(Integer maxUploads);

        Builder isTruncated(Boolean isTruncated);

        Builder uploads(List<MultipartUpload> uploads);

        Builder commonPrefixes(List<CommonPrefix> commonPrefixes);

        Builder encodingType(EncodingType encodingType);

        ListMultipartUploadsOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket to which the multipart upload was initiated.</p>
         */
        String bucket;

        /**
         * <p>The key at or after which the listing began.</p>
         */
        String keyMarker;

        /**
         * <p>Upload ID after which listing began.</p>
         */
        String uploadIdMarker;

        /**
         * <p>When a list is truncated, this element specifies the value that should be used for the
         *          key-marker request parameter in a subsequent request.</p>
         */
        String nextKeyMarker;

        /**
         * <p>When a prefix is provided in the request, this field contains the specified prefix. The
         *          result contains only keys starting with the specified prefix.</p>
         */
        String prefix;

        /**
         * <p>Contains the delimiter you specified in the request. If you don't specify a delimiter in
         *          your request, this element is absent from the response.</p>
         */
        String delimiter;

        /**
         * <p>When a list is truncated, this element specifies the value that should be used for the
         *             <code>upload-id-marker</code> request parameter in a subsequent request.</p>
         */
        String nextUploadIdMarker;

        /**
         * <p>Maximum number of multipart uploads that could have been included in the
         *          response.</p>
         */
        Integer maxUploads;

        /**
         * <p>Indicates whether the returned list of multipart uploads is truncated. A value of true
         *          indicates that the list was truncated. The list can be truncated if the number of multipart
         *          uploads exceeds the limit allowed or specified by max uploads.</p>
         */
        Boolean isTruncated;

        /**
         * <p>Container for elements related to a particular multipart upload. A response can contain
         *          zero or more <code>Upload</code> elements.</p>
         */
        List<MultipartUpload> uploads;

        /**
         * <p>If you specify a delimiter in the request, then the result returns each distinct key
         *          prefix containing the delimiter in a <code>CommonPrefixes</code> element. The distinct key
         *          prefixes are returned in the <code>Prefix</code> child element.</p>
         */
        List<CommonPrefix> commonPrefixes;

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
        EncodingType encodingType;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListMultipartUploadsOutput model) {
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
            return new ListMultipartUploadsOutput(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder keyMarker(String keyMarker) {
            this.keyMarker = keyMarker;
            return this;
        }

        public final Builder uploadIdMarker(String uploadIdMarker) {
            this.uploadIdMarker = uploadIdMarker;
            return this;
        }

        public final Builder nextKeyMarker(String nextKeyMarker) {
            this.nextKeyMarker = nextKeyMarker;
            return this;
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public final Builder nextUploadIdMarker(String nextUploadIdMarker) {
            this.nextUploadIdMarker = nextUploadIdMarker;
            return this;
        }

        public final Builder maxUploads(Integer maxUploads) {
            this.maxUploads = maxUploads;
            return this;
        }

        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        public final Builder uploads(List<MultipartUpload> uploads) {
            this.uploads = uploads;
            return this;
        }

        public final Builder commonPrefixes(List<CommonPrefix> commonPrefixes) {
            this.commonPrefixes = commonPrefixes;
            return this;
        }

        public final Builder encodingType(EncodingType encodingType) {
            this.encodingType = encodingType;
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

        public String keyMarker() {
            return keyMarker;
        }

        public String uploadIdMarker() {
            return uploadIdMarker;
        }

        public String nextKeyMarker() {
            return nextKeyMarker;
        }

        public String prefix() {
            return prefix;
        }

        public String delimiter() {
            return delimiter;
        }

        public String nextUploadIdMarker() {
            return nextUploadIdMarker;
        }

        public Integer maxUploads() {
            return maxUploads;
        }

        public Boolean isTruncated() {
            return isTruncated;
        }

        public List<MultipartUpload> uploads() {
            return uploads;
        }

        public List<CommonPrefix> commonPrefixes() {
            return commonPrefixes;
        }

        public EncodingType encodingType() {
            return encodingType;
        }
    }
}
