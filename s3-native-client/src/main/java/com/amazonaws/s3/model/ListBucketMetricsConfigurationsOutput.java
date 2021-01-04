// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListBucketMetricsConfigurationsOutput {
    /**
     * <p>Indicates whether the returned list of metrics configurations is complete. A value of
     *          true indicates that the list is not complete and the NextContinuationToken will be provided
     *          for a subsequent request.</p>
     */
    Boolean isTruncated;

    /**
     * <p>The marker that is used as a starting point for this metrics configuration list
     *          response. This value is present if it was sent in the request.</p>
     */
    String continuationToken;

    /**
     * <p>The marker used to continue a metrics configuration listing that has been truncated. Use
     *          the <code>NextContinuationToken</code> from a previously truncated list response to
     *          continue the listing. The continuation token is an opaque value that Amazon S3
     *          understands.</p>
     */
    String nextContinuationToken;

    /**
     * <p>The list of metrics configurations for a bucket.</p>
     */
    List<MetricsConfiguration> metricsConfigurationList;

    ListBucketMetricsConfigurationsOutput() {
        this.isTruncated = null;
        this.continuationToken = "";
        this.nextContinuationToken = "";
        this.metricsConfigurationList = null;
    }

    protected ListBucketMetricsConfigurationsOutput(BuilderImpl builder) {
        this.isTruncated = builder.isTruncated;
        this.continuationToken = builder.continuationToken;
        this.nextContinuationToken = builder.nextContinuationToken;
        this.metricsConfigurationList = builder.metricsConfigurationList;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ListBucketMetricsConfigurationsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListBucketMetricsConfigurationsOutput);
    }

    public Boolean isTruncated() {
        return isTruncated;
    }

    public String continuationToken() {
        return continuationToken;
    }

    public String nextContinuationToken() {
        return nextContinuationToken;
    }

    public List<MetricsConfiguration> metricsConfigurationList() {
        return metricsConfigurationList;
    }

    public void setIsTruncated(final Boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public void setContinuationToken(final String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public void setNextContinuationToken(final String nextContinuationToken) {
        this.nextContinuationToken = nextContinuationToken;
    }

    public void setMetricsConfigurationList(
            final List<MetricsConfiguration> metricsConfigurationList) {
        this.metricsConfigurationList = metricsConfigurationList;
    }

    public interface Builder {
        Builder isTruncated(Boolean isTruncated);

        Builder continuationToken(String continuationToken);

        Builder nextContinuationToken(String nextContinuationToken);

        Builder metricsConfigurationList(List<MetricsConfiguration> metricsConfigurationList);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Indicates whether the returned list of metrics configurations is complete. A value of
         *          true indicates that the list is not complete and the NextContinuationToken will be provided
         *          for a subsequent request.</p>
         */
        Boolean isTruncated;

        /**
         * <p>The marker that is used as a starting point for this metrics configuration list
         *          response. This value is present if it was sent in the request.</p>
         */
        String continuationToken;

        /**
         * <p>The marker used to continue a metrics configuration listing that has been truncated. Use
         *          the <code>NextContinuationToken</code> from a previously truncated list response to
         *          continue the listing. The continuation token is an opaque value that Amazon S3
         *          understands.</p>
         */
        String nextContinuationToken;

        /**
         * <p>The list of metrics configurations for a bucket.</p>
         */
        List<MetricsConfiguration> metricsConfigurationList;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListBucketMetricsConfigurationsOutput model) {
            isTruncated(model.isTruncated);
            continuationToken(model.continuationToken);
            nextContinuationToken(model.nextContinuationToken);
            metricsConfigurationList(model.metricsConfigurationList);
        }

        public ListBucketMetricsConfigurationsOutput build() {
            return new ListBucketMetricsConfigurationsOutput(this);
        }

        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
            return this;
        }

        public final Builder nextContinuationToken(String nextContinuationToken) {
            this.nextContinuationToken = nextContinuationToken;
            return this;
        }

        public final Builder metricsConfigurationList(
                List<MetricsConfiguration> metricsConfigurationList) {
            this.metricsConfigurationList = metricsConfigurationList;
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

        public String continuationToken() {
            return continuationToken;
        }

        public String nextContinuationToken() {
            return nextContinuationToken;
        }

        public List<MetricsConfiguration> metricsConfigurationList() {
            return metricsConfigurationList;
        }

        public void setIsTruncated(final Boolean isTruncated) {
            this.isTruncated = isTruncated;
        }

        public void setContinuationToken(final String continuationToken) {
            this.continuationToken = continuationToken;
        }

        public void setNextContinuationToken(final String nextContinuationToken) {
            this.nextContinuationToken = nextContinuationToken;
        }

        public void setMetricsConfigurationList(
                final List<MetricsConfiguration> metricsConfigurationList) {
            this.metricsConfigurationList = metricsConfigurationList;
        }
    }
}
