// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListBucketMetricsConfigurationsOutput {
    private Boolean isTruncated;

    private String continuationToken;

    private String nextContinuationToken;

    private List<MetricsConfiguration> metricsConfigurationList;

    private ListBucketMetricsConfigurationsOutput() {
        this.isTruncated = null;
        this.continuationToken = null;
        this.nextContinuationToken = null;
        this.metricsConfigurationList = null;
    }

    private ListBucketMetricsConfigurationsOutput(Builder builder) {
        this.isTruncated = builder.isTruncated;
        this.continuationToken = builder.continuationToken;
        this.nextContinuationToken = builder.nextContinuationToken;
        this.metricsConfigurationList = builder.metricsConfigurationList;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setIsTruncated(final Boolean isTruncated) {
        this.isTruncated = isTruncated;
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

    public List<MetricsConfiguration> metricsConfigurationList() {
        return metricsConfigurationList;
    }

    public void setMetricsConfigurationList(
            final List<MetricsConfiguration> metricsConfigurationList) {
        this.metricsConfigurationList = metricsConfigurationList;
    }

    static final class Builder {
        private Boolean isTruncated;

        private String continuationToken;

        private String nextContinuationToken;

        private List<MetricsConfiguration> metricsConfigurationList;

        private Builder() {
        }

        private Builder(ListBucketMetricsConfigurationsOutput model) {
            isTruncated(model.isTruncated);
            continuationToken(model.continuationToken);
            nextContinuationToken(model.nextContinuationToken);
            metricsConfigurationList(model.metricsConfigurationList);
        }

        public ListBucketMetricsConfigurationsOutput build() {
            return new com.amazonaws.s3.model.ListBucketMetricsConfigurationsOutput(this);
        }

        /**
         * <p>Indicates whether the returned list of metrics configurations is complete. A value of
         *          true indicates that the list is not complete and the NextContinuationToken will be provided
         *          for a subsequent request.</p>
         */
        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        /**
         * <p>The marker that is used as a starting point for this metrics configuration list
         *          response. This value is present if it was sent in the request.</p>
         */
        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
            return this;
        }

        /**
         * <p>The marker used to continue a metrics configuration listing that has been truncated. Use
         *          the <code>NextContinuationToken</code> from a previously truncated list response to
         *          continue the listing. The continuation token is an opaque value that Amazon S3
         *          understands.</p>
         */
        public final Builder nextContinuationToken(String nextContinuationToken) {
            this.nextContinuationToken = nextContinuationToken;
            return this;
        }

        /**
         * <p>The list of metrics configurations for a bucket.</p>
         */
        public final Builder metricsConfigurationList(
                List<MetricsConfiguration> metricsConfigurationList) {
            this.metricsConfigurationList = metricsConfigurationList;
            return this;
        }
    }
}
