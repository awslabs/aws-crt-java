// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListObjectsOutput {
    private Boolean isTruncated;

    private String marker;

    private String nextMarker;

    private List<Object> contents;

    private String name;

    private String prefix;

    private String delimiter;

    private Integer maxKeys;

    private List<CommonPrefix> commonPrefixes;

    private EncodingType encodingType;

    private ListObjectsOutput() {
        this.isTruncated = null;
        this.marker = null;
        this.nextMarker = null;
        this.contents = null;
        this.name = null;
        this.prefix = null;
        this.delimiter = null;
        this.maxKeys = null;
        this.commonPrefixes = null;
        this.encodingType = null;
    }

    private ListObjectsOutput(Builder builder) {
        this.isTruncated = builder.isTruncated;
        this.marker = builder.marker;
        this.nextMarker = builder.nextMarker;
        this.contents = builder.contents;
        this.name = builder.name;
        this.prefix = builder.prefix;
        this.delimiter = builder.delimiter;
        this.maxKeys = builder.maxKeys;
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
        return Objects.hash(ListObjectsOutput.class);
    }

    @Override
    public boolean equals(java.lang.Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListObjectsOutput);
    }

    public Boolean isTruncated() {
        return isTruncated;
    }

    public void setIsTruncated(final Boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public String marker() {
        return marker;
    }

    public void setMarker(final String marker) {
        this.marker = marker;
    }

    public String nextMarker() {
        return nextMarker;
    }

    public void setNextMarker(final String nextMarker) {
        this.nextMarker = nextMarker;
    }

    public List<Object> contents() {
        return contents;
    }

    public void setContents(final List<Object> contents) {
        this.contents = contents;
    }

    public String name() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

    public Integer maxKeys() {
        return maxKeys;
    }

    public void setMaxKeys(final Integer maxKeys) {
        this.maxKeys = maxKeys;
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
        private Boolean isTruncated;

        private String marker;

        private String nextMarker;

        private List<Object> contents;

        private String name;

        private String prefix;

        private String delimiter;

        private Integer maxKeys;

        private List<CommonPrefix> commonPrefixes;

        private EncodingType encodingType;

        private Builder() {
        }

        private Builder(ListObjectsOutput model) {
            isTruncated(model.isTruncated);
            marker(model.marker);
            nextMarker(model.nextMarker);
            contents(model.contents);
            name(model.name);
            prefix(model.prefix);
            delimiter(model.delimiter);
            maxKeys(model.maxKeys);
            commonPrefixes(model.commonPrefixes);
            encodingType(model.encodingType);
        }

        public ListObjectsOutput build() {
            return new com.amazonaws.s3.model.ListObjectsOutput(this);
        }

        /**
         * <p>A flag that indicates whether Amazon S3 returned all of the results that satisfied the search
         *          criteria.</p>
         */
        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        /**
         * <p>Indicates where in the bucket listing begins. Marker is included in the response if it
         *          was sent with the request.</p>
         */
        public final Builder marker(String marker) {
            this.marker = marker;
            return this;
        }

        /**
         * <p>When response is truncated (the IsTruncated element value in the response is true), you
         *          can use the key name in this field as marker in the subsequent request to get next set of
         *          objects. Amazon S3 lists objects in alphabetical order Note: This element is returned only if
         *          you have delimiter request parameter specified. If response does not include the NextMarker
         *          and it is truncated, you can use the value of the last Key in the response as the marker in
         *          the subsequent request to get the next set of object keys.</p>
         */
        public final Builder nextMarker(String nextMarker) {
            this.nextMarker = nextMarker;
            return this;
        }

        /**
         * <p>Metadata about each object returned.</p>
         */
        public final Builder contents(List<Object> contents) {
            this.contents = contents;
            return this;
        }

        /**
         * <p>The bucket name.</p>
         */
        public final Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * <p>Keys that begin with the indicated prefix.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>Causes keys that contain the same string between the prefix and the first occurrence of
         *          the delimiter to be rolled up into a single result element in the
         *             <code>CommonPrefixes</code> collection. These rolled-up keys are not returned elsewhere
         *          in the response. Each rolled-up result counts as only one return against the
         *             <code>MaxKeys</code> value.</p>
         */
        public final Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * <p>The maximum number of keys returned in the response body.</p>
         */
        public final Builder maxKeys(Integer maxKeys) {
            this.maxKeys = maxKeys;
            return this;
        }

        /**
         * <p>All of the keys rolled up in a common prefix count as a single return when calculating
         *          the number of returns. </p>
         *
         *          <p>A response can contain CommonPrefixes only if you specify a delimiter.</p>
         *
         *          <p>CommonPrefixes contains all (if there are any) keys between Prefix and the next
         *          occurrence of the string specified by the delimiter.</p>
         *
         *          <p> CommonPrefixes lists keys that act like subdirectories in the directory specified by
         *          Prefix.</p>
         *
         *          <p>For example, if the prefix is notes/ and the delimiter is a slash (/) as in
         *          notes/summer/july, the common prefix is notes/summer/. All of the keys that roll up into a
         *          common prefix count as a single return when calculating the number of returns.</p>
         */
        public final Builder commonPrefixes(List<CommonPrefix> commonPrefixes) {
            this.commonPrefixes = commonPrefixes;
            return this;
        }

        /**
         * <p>Encoding type used by Amazon S3 to encode object keys in the response.</p>
         */
        public final Builder encodingType(EncodingType encodingType) {
            this.encodingType = encodingType;
            return this;
        }
    }
}
