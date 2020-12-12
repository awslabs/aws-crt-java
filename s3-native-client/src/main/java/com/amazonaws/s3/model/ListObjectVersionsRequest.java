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
public class ListObjectVersionsRequest {
    private String bucket;

    private String delimiter;

    private EncodingType encodingType;

    private String keyMarker;

    private Integer maxKeys;

    private String prefix;

    private String versionIdMarker;

    private String expectedBucketOwner;

    private ListObjectVersionsRequest() {
        this.bucket = null;
        this.delimiter = null;
        this.encodingType = null;
        this.keyMarker = null;
        this.maxKeys = null;
        this.prefix = null;
        this.versionIdMarker = null;
        this.expectedBucketOwner = null;
    }

    private ListObjectVersionsRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.delimiter = builder.delimiter;
        this.encodingType = builder.encodingType;
        this.keyMarker = builder.keyMarker;
        this.maxKeys = builder.maxKeys;
        this.prefix = builder.prefix;
        this.versionIdMarker = builder.versionIdMarker;
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

    public String keyMarker() {
        return keyMarker;
    }

    public void setKeyMarker(final String keyMarker) {
        this.keyMarker = keyMarker;
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

    public String versionIdMarker() {
        return versionIdMarker;
    }

    public void setVersionIdMarker(final String versionIdMarker) {
        this.versionIdMarker = versionIdMarker;
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

        private String keyMarker;

        private Integer maxKeys;

        private String prefix;

        private String versionIdMarker;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(ListObjectVersionsRequest model) {
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
            return new com.amazonaws.s3.model.ListObjectVersionsRequest(this);
        }

        /**
         * <p>The bucket name that contains the objects. </p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>A delimiter is a character that you specify to group keys. All keys that contain the
         *          same string between the <code>prefix</code> and the first occurrence of the delimiter are
         *          grouped under a single result element in CommonPrefixes. These groups are counted as one
         *          result against the max-keys limitation. These keys are not returned elsewhere in the
         *          response.</p>
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
        public final Builder keyMarker(String keyMarker) {
            this.keyMarker = keyMarker;
            return this;
        }

        /**
         * <p>Sets the maximum number of keys returned in the response. By default the API returns up
         *          to 1,000 key names. The response might contain fewer keys but will never contain more. If
         *          additional keys satisfy the search criteria, but were not returned because max-keys was
         *          exceeded, the response contains <isTruncated>true</isTruncated>. To return the
         *          additional keys, see key-marker and version-id-marker.</p>
         */
        public final Builder maxKeys(Integer maxKeys) {
            this.maxKeys = maxKeys;
            return this;
        }

        /**
         * <p>Use this parameter to select only those keys that begin with the specified prefix. You
         *          can use prefixes to separate a bucket into different groupings of keys. (You can think of
         *          using prefix to make groups in the same way you'd use a folder in a file system.) You can
         *          use prefix with delimiter to roll up numerous objects into a single result under
         *          CommonPrefixes. </p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>Specifies the object version you want to start listing from.</p>
         */
        public final Builder versionIdMarker(String versionIdMarker) {
            this.versionIdMarker = versionIdMarker;
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
