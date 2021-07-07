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
public class ListObjectVersionsOutput {
    /**
     * <p>A flag that indicates whether Amazon S3 returned all of the results that satisfied the search
     *          criteria. If your results were truncated, you can make a follow-up paginated request using
     *          the NextKeyMarker and NextVersionIdMarker response parameters as a starting place in
     *          another request to return the rest of the results.</p>
     */
    Boolean isTruncated;

    /**
     * <p>Marks the last key returned in a truncated response.</p>
     */
    String keyMarker;

    /**
     * <p>Marks the last version of the key returned in a truncated response.</p>
     */
    String versionIdMarker;

    /**
     * <p>When the number of responses exceeds the value of <code>MaxKeys</code>,
     *             <code>NextKeyMarker</code> specifies the first key not returned that satisfies the
     *          search criteria. Use this value for the key-marker request parameter in a subsequent
     *          request.</p>
     */
    String nextKeyMarker;

    /**
     * <p>When the number of responses exceeds the value of <code>MaxKeys</code>,
     *             <code>NextVersionIdMarker</code> specifies the first object version not returned that
     *          satisfies the search criteria. Use this value for the version-id-marker request parameter
     *          in a subsequent request.</p>
     */
    String nextVersionIdMarker;

    /**
     * <p>Container for version information.</p>
     */
    List<ObjectVersion> versions;

    /**
     * <p>Container for an object that is a delete marker.</p>
     */
    List<DeleteMarkerEntry> deleteMarkers;

    /**
     * <p>The bucket name.</p>
     */
    String name;

    /**
     * <p>Selects objects that start with the value supplied by this parameter.</p>
     */
    String prefix;

    /**
     * <p>The delimiter grouping the included keys. A delimiter is a character that you specify to
     *          group keys. All keys that contain the same string between the prefix and the first
     *          occurrence of the delimiter are grouped under a single result element in
     *             <code>CommonPrefixes</code>. These groups are counted as one result against the max-keys
     *          limitation. These keys are not returned elsewhere in the response.</p>
     */
    String delimiter;

    /**
     * <p>Specifies the maximum number of objects to return.</p>
     */
    Integer maxKeys;

    /**
     * <p>All of the keys rolled up into a common prefix count as a single return when calculating
     *          the number of returns.</p>
     */
    List<CommonPrefix> commonPrefixes;

    /**
     * <p> Encoding type used by Amazon S3 to encode object key names in the XML response.</p>
     *
     *          <p>If you specify encoding-type request parameter, Amazon S3 includes this element in the
     *          response, and returns encoded key name values in the following response elements:</p>
     *
     *          <p>
     *             <code>KeyMarker, NextKeyMarker, Prefix, Key</code>, and <code>Delimiter</code>.</p>
     */
    EncodingType encodingType;

    ListObjectVersionsOutput() {
        this.isTruncated = null;
        this.keyMarker = "";
        this.versionIdMarker = "";
        this.nextKeyMarker = "";
        this.nextVersionIdMarker = "";
        this.versions = null;
        this.deleteMarkers = null;
        this.name = "";
        this.prefix = "";
        this.delimiter = "";
        this.maxKeys = null;
        this.commonPrefixes = null;
        this.encodingType = null;
    }

    protected ListObjectVersionsOutput(BuilderImpl builder) {
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

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String keyMarker() {
        return keyMarker;
    }

    public String versionIdMarker() {
        return versionIdMarker;
    }

    public String nextKeyMarker() {
        return nextKeyMarker;
    }

    public String nextVersionIdMarker() {
        return nextVersionIdMarker;
    }

    public List<ObjectVersion> versions() {
        return versions;
    }

    public List<DeleteMarkerEntry> deleteMarkers() {
        return deleteMarkers;
    }

    public String name() {
        return name;
    }

    public String prefix() {
        return prefix;
    }

    public String delimiter() {
        return delimiter;
    }

    public Integer maxKeys() {
        return maxKeys;
    }

    public List<CommonPrefix> commonPrefixes() {
        return commonPrefixes;
    }

    public EncodingType encodingType() {
        return encodingType;
    }

    public interface Builder {
        Builder isTruncated(Boolean isTruncated);

        Builder keyMarker(String keyMarker);

        Builder versionIdMarker(String versionIdMarker);

        Builder nextKeyMarker(String nextKeyMarker);

        Builder nextVersionIdMarker(String nextVersionIdMarker);

        Builder versions(List<ObjectVersion> versions);

        Builder deleteMarkers(List<DeleteMarkerEntry> deleteMarkers);

        Builder name(String name);

        Builder prefix(String prefix);

        Builder delimiter(String delimiter);

        Builder maxKeys(Integer maxKeys);

        Builder commonPrefixes(List<CommonPrefix> commonPrefixes);

        Builder encodingType(EncodingType encodingType);

        ListObjectVersionsOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>A flag that indicates whether Amazon S3 returned all of the results that satisfied the search
         *          criteria. If your results were truncated, you can make a follow-up paginated request using
         *          the NextKeyMarker and NextVersionIdMarker response parameters as a starting place in
         *          another request to return the rest of the results.</p>
         */
        Boolean isTruncated;

        /**
         * <p>Marks the last key returned in a truncated response.</p>
         */
        String keyMarker;

        /**
         * <p>Marks the last version of the key returned in a truncated response.</p>
         */
        String versionIdMarker;

        /**
         * <p>When the number of responses exceeds the value of <code>MaxKeys</code>,
         *             <code>NextKeyMarker</code> specifies the first key not returned that satisfies the
         *          search criteria. Use this value for the key-marker request parameter in a subsequent
         *          request.</p>
         */
        String nextKeyMarker;

        /**
         * <p>When the number of responses exceeds the value of <code>MaxKeys</code>,
         *             <code>NextVersionIdMarker</code> specifies the first object version not returned that
         *          satisfies the search criteria. Use this value for the version-id-marker request parameter
         *          in a subsequent request.</p>
         */
        String nextVersionIdMarker;

        /**
         * <p>Container for version information.</p>
         */
        List<ObjectVersion> versions;

        /**
         * <p>Container for an object that is a delete marker.</p>
         */
        List<DeleteMarkerEntry> deleteMarkers;

        /**
         * <p>The bucket name.</p>
         */
        String name;

        /**
         * <p>Selects objects that start with the value supplied by this parameter.</p>
         */
        String prefix;

        /**
         * <p>The delimiter grouping the included keys. A delimiter is a character that you specify to
         *          group keys. All keys that contain the same string between the prefix and the first
         *          occurrence of the delimiter are grouped under a single result element in
         *             <code>CommonPrefixes</code>. These groups are counted as one result against the max-keys
         *          limitation. These keys are not returned elsewhere in the response.</p>
         */
        String delimiter;

        /**
         * <p>Specifies the maximum number of objects to return.</p>
         */
        Integer maxKeys;

        /**
         * <p>All of the keys rolled up into a common prefix count as a single return when calculating
         *          the number of returns.</p>
         */
        List<CommonPrefix> commonPrefixes;

        /**
         * <p> Encoding type used by Amazon S3 to encode object key names in the XML response.</p>
         *
         *          <p>If you specify encoding-type request parameter, Amazon S3 includes this element in the
         *          response, and returns encoded key name values in the following response elements:</p>
         *
         *          <p>
         *             <code>KeyMarker, NextKeyMarker, Prefix, Key</code>, and <code>Delimiter</code>.</p>
         */
        EncodingType encodingType;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListObjectVersionsOutput model) {
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
            return new ListObjectVersionsOutput(this);
        }

        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        public final Builder keyMarker(String keyMarker) {
            this.keyMarker = keyMarker;
            return this;
        }

        public final Builder versionIdMarker(String versionIdMarker) {
            this.versionIdMarker = versionIdMarker;
            return this;
        }

        public final Builder nextKeyMarker(String nextKeyMarker) {
            this.nextKeyMarker = nextKeyMarker;
            return this;
        }

        public final Builder nextVersionIdMarker(String nextVersionIdMarker) {
            this.nextVersionIdMarker = nextVersionIdMarker;
            return this;
        }

        public final Builder versions(List<ObjectVersion> versions) {
            this.versions = versions;
            return this;
        }

        public final Builder deleteMarkers(List<DeleteMarkerEntry> deleteMarkers) {
            this.deleteMarkers = deleteMarkers;
            return this;
        }

        public final Builder name(String name) {
            this.name = name;
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

        public final Builder maxKeys(Integer maxKeys) {
            this.maxKeys = maxKeys;
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

        public Boolean isTruncated() {
            return isTruncated;
        }

        public String keyMarker() {
            return keyMarker;
        }

        public String versionIdMarker() {
            return versionIdMarker;
        }

        public String nextKeyMarker() {
            return nextKeyMarker;
        }

        public String nextVersionIdMarker() {
            return nextVersionIdMarker;
        }

        public List<ObjectVersion> versions() {
            return versions;
        }

        public List<DeleteMarkerEntry> deleteMarkers() {
            return deleteMarkers;
        }

        public String name() {
            return name;
        }

        public String prefix() {
            return prefix;
        }

        public String delimiter() {
            return delimiter;
        }

        public Integer maxKeys() {
            return maxKeys;
        }

        public List<CommonPrefix> commonPrefixes() {
            return commonPrefixes;
        }

        public EncodingType encodingType() {
            return encodingType;
        }
    }
}
