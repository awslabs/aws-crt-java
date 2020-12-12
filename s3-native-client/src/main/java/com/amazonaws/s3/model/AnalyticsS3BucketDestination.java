// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AnalyticsS3BucketDestination {
    private AnalyticsS3ExportFileFormat format;

    private String bucketAccountId;

    private String bucket;

    private String prefix;

    private AnalyticsS3BucketDestination() {
        this.format = null;
        this.bucketAccountId = null;
        this.bucket = null;
        this.prefix = null;
    }

    private AnalyticsS3BucketDestination(Builder builder) {
        this.format = builder.format;
        this.bucketAccountId = builder.bucketAccountId;
        this.bucket = builder.bucket;
        this.prefix = builder.prefix;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(AnalyticsS3BucketDestination.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof AnalyticsS3BucketDestination);
    }

    public AnalyticsS3ExportFileFormat format() {
        return format;
    }

    public void setFormat(final AnalyticsS3ExportFileFormat format) {
        this.format = format;
    }

    public String bucketAccountId() {
        return bucketAccountId;
    }

    public void setBucketAccountId(final String bucketAccountId) {
        this.bucketAccountId = bucketAccountId;
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    static final class Builder {
        private AnalyticsS3ExportFileFormat format;

        private String bucketAccountId;

        private String bucket;

        private String prefix;

        private Builder() {
        }

        private Builder(AnalyticsS3BucketDestination model) {
            format(model.format);
            bucketAccountId(model.bucketAccountId);
            bucket(model.bucket);
            prefix(model.prefix);
        }

        public AnalyticsS3BucketDestination build() {
            return new com.amazonaws.s3.model.AnalyticsS3BucketDestination(this);
        }

        /**
         * <p>Specifies the file format used when exporting data to Amazon S3.</p>
         */
        public final Builder format(AnalyticsS3ExportFileFormat format) {
            this.format = format;
            return this;
        }

        /**
         * <p>The account ID that owns the destination S3 bucket. If no account ID is provided, the
         *          owner is not validated before exporting data.</p>
         *          <note>
         *             <p> Although this value is optional, we strongly recommend that you set it to help
         *             prevent problems if the destination bucket ownership changes. </p>
         *          </note>
         */
        public final Builder bucketAccountId(String bucketAccountId) {
            this.bucketAccountId = bucketAccountId;
            return this;
        }

        /**
         * <p>The Amazon Resource Name (ARN) of the bucket to which data is exported.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The prefix to use when exporting data. The prefix is prepended to all results.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }
    }
}
