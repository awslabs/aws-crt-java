// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AnalyticsS3BucketDestination {
    /**
     * <p>Specifies the file format used when exporting data to Amazon S3.</p>
     */
    AnalyticsS3ExportFileFormat format;

    /**
     * <p>The account ID that owns the destination S3 bucket. If no account ID is provided, the
     *          owner is not validated before exporting data.</p>
     *          <note>
     *             <p> Although this value is optional, we strongly recommend that you set it to help
     *             prevent problems if the destination bucket ownership changes. </p>
     *          </note>
     */
    String bucketAccountId;

    /**
     * <p>The Amazon Resource Name (ARN) of the bucket to which data is exported.</p>
     */
    String bucket;

    /**
     * <p>The prefix to use when exporting data. The prefix is prepended to all results.</p>
     */
    String prefix;

    AnalyticsS3BucketDestination() {
        this.format = null;
        this.bucketAccountId = "";
        this.bucket = "";
        this.prefix = "";
    }

    protected AnalyticsS3BucketDestination(BuilderImpl builder) {
        this.format = builder.format;
        this.bucketAccountId = builder.bucketAccountId;
        this.bucket = builder.bucket;
        this.prefix = builder.prefix;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String bucketAccountId() {
        return bucketAccountId;
    }

    public String bucket() {
        return bucket;
    }

    public String prefix() {
        return prefix;
    }

    public void setFormat(final AnalyticsS3ExportFileFormat format) {
        this.format = format;
    }

    public void setBucketAccountId(final String bucketAccountId) {
        this.bucketAccountId = bucketAccountId;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public interface Builder {
        Builder format(AnalyticsS3ExportFileFormat format);

        Builder bucketAccountId(String bucketAccountId);

        Builder bucket(String bucket);

        Builder prefix(String prefix);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the file format used when exporting data to Amazon S3.</p>
         */
        AnalyticsS3ExportFileFormat format;

        /**
         * <p>The account ID that owns the destination S3 bucket. If no account ID is provided, the
         *          owner is not validated before exporting data.</p>
         *          <note>
         *             <p> Although this value is optional, we strongly recommend that you set it to help
         *             prevent problems if the destination bucket ownership changes. </p>
         *          </note>
         */
        String bucketAccountId;

        /**
         * <p>The Amazon Resource Name (ARN) of the bucket to which data is exported.</p>
         */
        String bucket;

        /**
         * <p>The prefix to use when exporting data. The prefix is prepended to all results.</p>
         */
        String prefix;

        protected BuilderImpl() {
        }

        private BuilderImpl(AnalyticsS3BucketDestination model) {
            format(model.format);
            bucketAccountId(model.bucketAccountId);
            bucket(model.bucket);
            prefix(model.prefix);
        }

        public AnalyticsS3BucketDestination build() {
            return new AnalyticsS3BucketDestination(this);
        }

        public final Builder format(AnalyticsS3ExportFileFormat format) {
            this.format = format;
            return this;
        }

        public final Builder bucketAccountId(String bucketAccountId) {
            this.bucketAccountId = bucketAccountId;
            return this;
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
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

        public AnalyticsS3ExportFileFormat format() {
            return format;
        }

        public String bucketAccountId() {
            return bucketAccountId;
        }

        public String bucket() {
            return bucket;
        }

        public String prefix() {
            return prefix;
        }

        public void setFormat(final AnalyticsS3ExportFileFormat format) {
            this.format = format;
        }

        public void setBucketAccountId(final String bucketAccountId) {
            this.bucketAccountId = bucketAccountId;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }
    }
}
