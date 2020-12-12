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
public class ListObjectsV2Output {
    private Boolean isTruncated;

    private List<Object> contents;

    private String name;

    private String prefix;

    private String delimiter;

    private Integer maxKeys;

    private List<CommonPrefix> commonPrefixes;

    private EncodingType encodingType;

    private Integer keyCount;

    private String continuationToken;

    private String nextContinuationToken;

    private String startAfter;

    private ListObjectsV2Output() {
        this.isTruncated = null;
        this.contents = null;
        this.name = null;
        this.prefix = null;
        this.delimiter = null;
        this.maxKeys = null;
        this.commonPrefixes = null;
        this.encodingType = null;
        this.keyCount = null;
        this.continuationToken = null;
        this.nextContinuationToken = null;
        this.startAfter = null;
    }

    private ListObjectsV2Output(Builder builder) {
        this.isTruncated = builder.isTruncated;
        this.contents = builder.contents;
        this.name = builder.name;
        this.prefix = builder.prefix;
        this.delimiter = builder.delimiter;
        this.maxKeys = builder.maxKeys;
        this.commonPrefixes = builder.commonPrefixes;
        this.encodingType = builder.encodingType;
        this.keyCount = builder.keyCount;
        this.continuationToken = builder.continuationToken;
        this.nextContinuationToken = builder.nextContinuationToken;
        this.startAfter = builder.startAfter;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ListObjectsV2Output.class);
    }

    @Override
    public boolean equals(java.lang.Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListObjectsV2Output);
    }

    public Boolean isTruncated() {
        return isTruncated;
    }

    public void setIsTruncated(final Boolean isTruncated) {
        this.isTruncated = isTruncated;
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

    public Integer keyCount() {
        return keyCount;
    }

    public void setKeyCount(final Integer keyCount) {
        this.keyCount = keyCount;
    }

    public String continuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(final String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public String nextContinuationToken() {
        return nextContinuationToken;
    }

    public void setNextContinuationToken(final String nextContinuationToken) {
        this.nextContinuationToken = nextContinuationToken;
    }

    public String startAfter() {
        return startAfter;
    }

    public void setStartAfter(final String startAfter) {
        this.startAfter = startAfter;
    }

    static final class Builder {
        private Boolean isTruncated;

        private List<Object> contents;

        private String name;

        private String prefix;

        private String delimiter;

        private Integer maxKeys;

        private List<CommonPrefix> commonPrefixes;

        private EncodingType encodingType;

        private Integer keyCount;

        private String continuationToken;

        private String nextContinuationToken;

        private String startAfter;

        private Builder() {
        }

        private Builder(ListObjectsV2Output model) {
            isTruncated(model.isTruncated);
            contents(model.contents);
            name(model.name);
            prefix(model.prefix);
            delimiter(model.delimiter);
            maxKeys(model.maxKeys);
            commonPrefixes(model.commonPrefixes);
            encodingType(model.encodingType);
            keyCount(model.keyCount);
            continuationToken(model.continuationToken);
            nextContinuationToken(model.nextContinuationToken);
            startAfter(model.startAfter);
        }

        public ListObjectsV2Output build() {
            return new com.amazonaws.s3.model.ListObjectsV2Output(this);
        }

        /**
         * <p>Set to false if all of the results were returned. Set to true if more keys are available
         *          to return. If the number of results exceeds that specified by MaxKeys, all of the results
         *          might not be returned.</p>
         */
        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
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
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * <p> Keys that begin with the indicated prefix.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>Causes keys that contain the same string between the prefix and the first occurrence of
         *          the delimiter to be rolled up into a single result element in the CommonPrefixes
         *          collection. These rolled-up keys are not returned elsewhere in the response. Each rolled-up
         *          result counts as only one return against the <code>MaxKeys</code> value.</p>
         */
        public final Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
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
         * <p>All of the keys rolled up into a common prefix count as a single return when calculating
         *          the number of returns.</p>
         *
         *          <p>A response can contain <code>CommonPrefixes</code> only if you specify a
         *          delimiter.</p>
         *
         *          <p>
         *             <code>CommonPrefixes</code> contains all (if there are any) keys between
         *             <code>Prefix</code> and the next occurrence of the string specified by a
         *          delimiter.</p>
         *
         *          <p>
         *             <code>CommonPrefixes</code> lists keys that act like subdirectories in the directory
         *          specified by <code>Prefix</code>.</p>
         *
         *          <p>For example, if the prefix is <code>notes/</code> and the delimiter is a slash
         *             (<code>/</code>) as in <code>notes/summer/july</code>, the common prefix is
         *             <code>notes/summer/</code>. All of the keys that roll up into a common prefix count as a
         *          single return when calculating the number of returns. </p>
         */
        public final Builder commonPrefixes(List<CommonPrefix> commonPrefixes) {
            this.commonPrefixes = commonPrefixes;
            return this;
        }

        /**
         * <p>Encoding type used by Amazon S3 to encode object key names in the XML response.</p>
         *
         *          <p>If you specify the encoding-type request parameter, Amazon S3 includes this element in the
         *          response, and returns encoded key name values in the following response elements:</p>
         *
         *          <p>
         *             <code>Delimiter, Prefix, Key,</code> and <code>StartAfter</code>.</p>
         */
        public final Builder encodingType(EncodingType encodingType) {
            this.encodingType = encodingType;
            return this;
        }

        /**
         * <p>KeyCount is the number of keys returned with this request. KeyCount will always be less
         *          than equals to MaxKeys field. Say you ask for 50 keys, your result will include less than
         *          equals 50 keys </p>
         */
        public final Builder keyCount(Integer keyCount) {
            this.keyCount = keyCount;
            return this;
        }

        /**
         * <p> If ContinuationToken was sent with the request, it is included in the response.</p>
         */
        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
            return this;
        }

        /**
         * <p>
         *             <code>NextContinuationToken</code> is sent when <code>isTruncated</code> is true, which
         *          means there are more keys in the bucket that can be listed. The next list requests to Amazon S3
         *          can be continued with this <code>NextContinuationToken</code>.
         *             <code>NextContinuationToken</code> is obfuscated and is not a real key</p>
         */
        public final Builder nextContinuationToken(String nextContinuationToken) {
            this.nextContinuationToken = nextContinuationToken;
            return this;
        }

        /**
         * <p>If StartAfter was sent with the request, it is included in the response.</p>
         */
        public final Builder startAfter(String startAfter) {
            this.startAfter = startAfter;
            return this;
        }
    }
}
