// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AnalyticsExportDestination {
    /**
     * <p>A destination signifying output to an S3 bucket.</p>
     */
    AnalyticsS3BucketDestination s3BucketDestination;

    AnalyticsExportDestination() {
        this.s3BucketDestination = null;
    }

    protected AnalyticsExportDestination(BuilderImpl builder) {
        this.s3BucketDestination = builder.s3BucketDestination;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder s3BucketDestination(AnalyticsS3BucketDestination s3BucketDestination);

        AnalyticsExportDestination build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>A destination signifying output to an S3 bucket.</p>
         */
        AnalyticsS3BucketDestination s3BucketDestination;

        protected BuilderImpl() {
        }

        private BuilderImpl(AnalyticsExportDestination model) {
            s3BucketDestination(model.s3BucketDestination);
        }

        public AnalyticsExportDestination build() {
            return new AnalyticsExportDestination(this);
        }

        public final Builder s3BucketDestination(AnalyticsS3BucketDestination s3BucketDestination) {
            this.s3BucketDestination = s3BucketDestination;
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

        public AnalyticsS3BucketDestination s3BucketDestination() {
            return s3BucketDestination;
        }
    }
}
