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
public class ListObjectVersionsOutput {
    private Boolean isTruncated;

    private String keyMarker;

    private String versionIdMarker;

    private String nextKeyMarker;

    private String nextVersionIdMarker;

    private List<ObjectVersion> versions;

    private List<DeleteMarkerEntry> deleteMarkers;

    private String name;

    private String prefix;

    private String delimiter;

    private Integer maxKeys;

    private List<CommonPrefix> commonPrefixes;

    private EncodingType encodingType;

    private ListObjectVersionsOutput() {
        this.isTruncated = null;
        this.keyMarker = null;
        this.versionIdMarker = null;
        this.nextKeyMarker = null;
        this.nextVersionIdMarker = null;
        this.versions = null;
        this.deleteMarkers = null;
        this.name = null;
        this.prefix = null;
        this.delimiter = null;
        this.maxKeys = null;
        this.commonPrefixes = null;
        this.encodingType = null;
    }

    private ListObjectVersionsOutput(Builder builder) {
        this.isTruncated = builder.isTruncated;
        this.keyMarker = builder.keyMarker;
        this.versionIdMarker = builder.versionIdMarker;
        this.nextKeyMarker = builder.nextKeyMarker;
        this.nextVersionIdMarker = builder.nextVersionIdMarker;
        this.versions = builder.versions;
        this.deleteMarkers = builder.deleteMarkers;
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
        return Objects.hash(ListObjectVersionsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListObjectVersionsOutput);
    }

    public Boolean isTruncated() {
        return isTruncated;
    }

    public void setIsTruncated(final Boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public String keyMarker() {
        return keyMarker;
    }

    public void setKeyMarker(final String keyMarker) {
        this.keyMarker = keyMarker;
    }

    public String versionIdMarker() {
        return versionIdMarker;
    }

    public void setVersionIdMarker(final String versionIdMarker) {
        this.versionIdMarker = versionIdMarker;
    }

    public String nextKeyMarker() {
        return nextKeyMarker;
    }

    public void setNextKeyMarker(final String nextKeyMarker) {
        this.nextKeyMarker = nextKeyMarker;
    }

    public String nextVersionIdMarker() {
        return nextVersionIdMarker;
    }

    public void setNextVersionIdMarker(final String nextVersionIdMarker) {
        this.nextVersionIdMarker = nextVersionIdMarker;
    }

    public List<ObjectVersion> versions() {
        return versions;
    }

    public void setVersions(final List<ObjectVersion> versions) {
        this.versions = versions;
    }

    public List<DeleteMarkerEntry> deleteMarkers() {
        return deleteMarkers;
    }

    public void setDeleteMarkers(final List<DeleteMarkerEntry> deleteMarkers) {
        this.deleteMarkers = deleteMarkers;
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

        private String keyMarker;

        private String versionIdMarker;

        private String nextKeyMarker;

        private String nextVersionIdMarker;

        private List<ObjectVersion> versions;

        private List<DeleteMarkerEntry> deleteMarkers;

        private String name;

        private String prefix;

        private String delimiter;

        private Integer maxKeys;

        private List<CommonPrefix> commonPrefixes;

        private EncodingType encodingType;

        private Builder() {
        }

        private Builder(ListObjectVersionsOutput model) {
            isTruncated(model.isTruncated);
            keyMarker(model.keyMarker);
            versionIdMarker(model.versionIdMarker);
            nextKeyMarker(model.nextKeyMarker);
            nextVersionIdMarker(model.nextVersionIdMarker);
            versions(model.versions);
            deleteMarkers(model.deleteMarkers);
            name(model.name);
            prefix(model.prefix);
            delimiter(model.delimiter);
            maxKeys(model.maxKeys);
            commonPrefixes(model.commonPrefixes);
            encodingType(model.encodingType);
        }

        public ListObjectVersionsOutput build() {
            return new com.amazonaws.s3.model.ListObjectVersionsOutput(this);
        }

        /**
         * <p>A flag that indicates whether Amazon S3 returned all of the results that satisfied the search
         *          criteria. If your results were truncated, you can make a follow-up paginated request using
         *          the NextKeyMarker and NextVersionIdMarker response parameters as a starting place in
         *          another request to return the rest of the results.</p>
         */
        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        /**
         * <p>Marks the last key returned in a truncated response.</p>
         */
        public final Builder keyMarker(String keyMarker) {
            this.keyMarker = keyMarker;
            return this;
        }

        /**
         * <p>Marks the last version of the key returned in a truncated response.</p>
         */
        public final Builder versionIdMarker(String versionIdMarker) {
            this.versionIdMarker = versionIdMarker;
            return this;
        }

        /**
         * <p>When the number of responses exceeds the value of <code>MaxKeys</code>,
         *             <code>NextKeyMarker</code> specifies the first key not returned that satisfies the
         *          search criteria. Use this value for the key-marker request parameter in a subsequent
         *          request.</p>
         */
        public final Builder nextKeyMarker(String nextKeyMarker) {
            this.nextKeyMarker = nextKeyMarker;
            return this;
        }

        /**
         * <p>When the number of responses exceeds the value of <code>MaxKeys</code>,
         *             <code>NextVersionIdMarker</code> specifies the first object version not returned that
         *          satisfies the search criteria. Use this value for the version-id-marker request parameter
         *          in a subsequent request.</p>
         */
        public final Builder nextVersionIdMarker(String nextVersionIdMarker) {
            this.nextVersionIdMarker = nextVersionIdMarker;
            return this;
        }

        /**
         * <p>Container for version information.</p>
         */
        public final Builder versions(List<ObjectVersion> versions) {
            this.versions = versions;
            return this;
        }

        /**
         * <p>Container for an object that is a delete marker.</p>
         */
        public final Builder deleteMarkers(List<DeleteMarkerEntry> deleteMarkers) {
            this.deleteMarkers = deleteMarkers;
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
         * <p>Selects objects that start with the value supplied by this parameter.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>The delimiter grouping the included keys. A delimiter is a character that you specify to
         *          group keys. All keys that contain the same string between the prefix and the first
         *          occurrence of the delimiter are grouped under a single result element in
         *             <code>CommonPrefixes</code>. These groups are counted as one result against the max-keys
         *          limitation. These keys are not returned elsewhere in the response.</p>
         */
        public final Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * <p>Specifies the maximum number of objects to return.</p>
         */
        public final Builder maxKeys(Integer maxKeys) {
            this.maxKeys = maxKeys;
            return this;
        }

        /**
         * <p>All of the keys rolled up into a common prefix count as a single return when calculating
         *          the number of returns.</p>
         */
        public final Builder commonPrefixes(List<CommonPrefix> commonPrefixes) {
            this.commonPrefixes = commonPrefixes;
            return this;
        }

        /**
         * <p> Encoding type used by Amazon S3 to encode object key names in the XML response.</p>
         *
         *          <p>If you specify encoding-type request parameter, Amazon S3 includes this element in the
         *          response, and returns encoded key name values in the following response elements:</p>
         *
         *          <p>
         *             <code>KeyMarker, NextKeyMarker, Prefix, Key</code>, and <code>Delimiter</code>.</p>
         */
        public final Builder encodingType(EncodingType encodingType) {
            this.encodingType = encodingType;
            return this;
        }
    }
}
