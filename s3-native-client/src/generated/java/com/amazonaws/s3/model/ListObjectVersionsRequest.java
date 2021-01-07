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
public class ListObjectVersionsRequest {
    /**
     * <p>The bucket name that contains the objects. </p>
     */
    String bucket;

    /**
     * <p>A delimiter is a character that you specify to group keys. All keys that contain the
     *          same string between the <code>prefix</code> and the first occurrence of the delimiter are
     *          grouped under a single result element in CommonPrefixes. These groups are counted as one
     *          result against the max-keys limitation. These keys are not returned elsewhere in the
     *          response.</p>
     */
    String delimiter;

    EncodingType encodingType;

    /**
     * <p>Specifies the key to start with when listing objects in a bucket.</p>
     */
    String keyMarker;

    /**
     * <p>Sets the maximum number of keys returned in the response. By default the API returns up
     *          to 1,000 key names. The response might contain fewer keys but will never contain more. If
     *          additional keys satisfy the search criteria, but were not returned because max-keys was
     *          exceeded, the response contains <isTruncated>true</isTruncated>. To return the
     *          additional keys, see key-marker and version-id-marker.</p>
     */
    Integer maxKeys;

    /**
     * <p>Use this parameter to select only those keys that begin with the specified prefix. You
     *          can use prefixes to separate a bucket into different groupings of keys. (You can think of
     *          using prefix to make groups in the same way you'd use a folder in a file system.) You can
     *          use prefix with delimiter to roll up numerous objects into a single result under
     *          CommonPrefixes. </p>
     */
    String prefix;

    /**
     * <p>Specifies the object version you want to start listing from.</p>
     */
    String versionIdMarker;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    ListObjectVersionsRequest() {
        this.bucket = "";
        this.delimiter = "";
        this.encodingType = null;
        this.keyMarker = "";
        this.maxKeys = null;
        this.prefix = "";
        this.versionIdMarker = "";
        this.expectedBucketOwner = "";
    }

    protected ListObjectVersionsRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.delimiter = builder.delimiter;
        this.encodingType = builder.encodingType;
        this.keyMarker = builder.keyMarker;
        this.maxKeys = builder.maxKeys;
        this.prefix = builder.prefix;
        this.versionIdMarker = builder.versionIdMarker;
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
        return Objects.hash(ListObjectVersionsRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListObjectVersionsRequest);
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

    public Integer maxKeys() {
        return maxKeys;
    }

    public String prefix() {
        return prefix;
    }

    public String versionIdMarker() {
        return versionIdMarker;
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

    public void setKeyMarker(final String keyMarker) {
        this.keyMarker = keyMarker;
    }

    public void setMaxKeys(final Integer maxKeys) {
        this.maxKeys = maxKeys;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setVersionIdMarker(final String versionIdMarker) {
        this.versionIdMarker = versionIdMarker;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder delimiter(String delimiter);

        Builder encodingType(EncodingType encodingType);

        Builder keyMarker(String keyMarker);

        Builder maxKeys(Integer maxKeys);

        Builder prefix(String prefix);

        Builder versionIdMarker(String versionIdMarker);

        Builder expectedBucketOwner(String expectedBucketOwner);

        ListObjectVersionsRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket name that contains the objects. </p>
         */
        String bucket;

        /**
         * <p>A delimiter is a character that you specify to group keys. All keys that contain the
         *          same string between the <code>prefix</code> and the first occurrence of the delimiter are
         *          grouped under a single result element in CommonPrefixes. These groups are counted as one
         *          result against the max-keys limitation. These keys are not returned elsewhere in the
         *          response.</p>
         */
        String delimiter;

        EncodingType encodingType;

        /**
         * <p>Specifies the key to start with when listing objects in a bucket.</p>
         */
        String keyMarker;

        /**
         * <p>Sets the maximum number of keys returned in the response. By default the API returns up
         *          to 1,000 key names. The response might contain fewer keys but will never contain more. If
         *          additional keys satisfy the search criteria, but were not returned because max-keys was
         *          exceeded, the response contains <isTruncated>true</isTruncated>. To return the
         *          additional keys, see key-marker and version-id-marker.</p>
         */
        Integer maxKeys;

        /**
         * <p>Use this parameter to select only those keys that begin with the specified prefix. You
         *          can use prefixes to separate a bucket into different groupings of keys. (You can think of
         *          using prefix to make groups in the same way you'd use a folder in a file system.) You can
         *          use prefix with delimiter to roll up numerous objects into a single result under
         *          CommonPrefixes. </p>
         */
        String prefix;

        /**
         * <p>Specifies the object version you want to start listing from.</p>
         */
        String versionIdMarker;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListObjectVersionsRequest model) {
            bucket(model.bucket);
            delimiter(model.delimiter);
            encodingType(model.encodingType);
            keyMarker(model.keyMarker);
            maxKeys(model.maxKeys);
            prefix(model.prefix);
            versionIdMarker(model.versionIdMarker);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public ListObjectVersionsRequest build() {
            return new ListObjectVersionsRequest(this);
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

        public final Builder maxKeys(Integer maxKeys) {
            this.maxKeys = maxKeys;
            return this;
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder versionIdMarker(String versionIdMarker) {
            this.versionIdMarker = versionIdMarker;
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

        public String keyMarker() {
            return keyMarker;
        }

        public Integer maxKeys() {
            return maxKeys;
        }

        public String prefix() {
            return prefix;
        }

        public String versionIdMarker() {
            return versionIdMarker;
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

        public void setKeyMarker(final String keyMarker) {
            this.keyMarker = keyMarker;
        }

        public void setMaxKeys(final Integer maxKeys) {
            this.maxKeys = maxKeys;
        }

        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }

        public void setVersionIdMarker(final String versionIdMarker) {
            this.versionIdMarker = versionIdMarker;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
