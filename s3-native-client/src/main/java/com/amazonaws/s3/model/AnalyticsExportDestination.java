// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AnalyticsExportDestination {
    private AnalyticsS3BucketDestination s3BucketDestination;

    private AnalyticsExportDestination() {
        this.s3BucketDestination = null;
    }

    private AnalyticsExportDestination(Builder builder) {
        this.s3BucketDestination = builder.s3BucketDestination;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(AnalyticsExportDestination.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof AnalyticsExportDestination);
    }

    public AnalyticsS3BucketDestination s3BucketDestination() {
        return s3BucketDestination;
    }

    public void setS3BucketDestination(final AnalyticsS3BucketDestination s3BucketDestination) {
        this.s3BucketDestination = s3BucketDestination;
    }

    static final class Builder {
        private AnalyticsS3BucketDestination s3BucketDestination;

        private Builder() {
        }

        private Builder(AnalyticsExportDestination model) {
            s3BucketDestination(model.s3BucketDestination);
        }

        public AnalyticsExportDestination build() {
            return new com.amazonaws.s3.model.AnalyticsExportDestination(this);
        }

        /**
         * <p>A destination signifying output to an S3 bucket.</p>
         */
        public final Builder s3BucketDestination(AnalyticsS3BucketDestination s3BucketDestination) {
            this.s3BucketDestination = s3BucketDestination;
            return this;
        }
    }
}
