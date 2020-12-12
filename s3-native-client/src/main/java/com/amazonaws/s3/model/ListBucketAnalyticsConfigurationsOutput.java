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
public class ListBucketAnalyticsConfigurationsOutput {
    private Boolean isTruncated;

    private String continuationToken;

    private String nextContinuationToken;

    private List<AnalyticsConfiguration> analyticsConfigurationList;

    private ListBucketAnalyticsConfigurationsOutput() {
        this.isTruncated = null;
        this.continuationToken = null;
        this.nextContinuationToken = null;
        this.analyticsConfigurationList = null;
    }

    private ListBucketAnalyticsConfigurationsOutput(Builder builder) {
        this.isTruncated = builder.isTruncated;
        this.continuationToken = builder.continuationToken;
        this.nextContinuationToken = builder.nextContinuationToken;
        this.analyticsConfigurationList = builder.analyticsConfigurationList;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ListBucketAnalyticsConfigurationsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListBucketAnalyticsConfigurationsOutput);
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

    public List<AnalyticsConfiguration> analyticsConfigurationList() {
        return analyticsConfigurationList;
    }

    public void setAnalyticsConfigurationList(
            final List<AnalyticsConfiguration> analyticsConfigurationList) {
        this.analyticsConfigurationList = analyticsConfigurationList;
    }

    static final class Builder {
        private Boolean isTruncated;

        private String continuationToken;

        private String nextContinuationToken;

        private List<AnalyticsConfiguration> analyticsConfigurationList;

        private Builder() {
        }

        private Builder(ListBucketAnalyticsConfigurationsOutput model) {
            isTruncated(model.isTruncated);
            continuationToken(model.continuationToken);
            nextContinuationToken(model.nextContinuationToken);
            analyticsConfigurationList(model.analyticsConfigurationList);
        }

        public ListBucketAnalyticsConfigurationsOutput build() {
            return new com.amazonaws.s3.model.ListBucketAnalyticsConfigurationsOutput(this);
        }

        /**
         * <p>Indicates whether the returned list of analytics configurations is complete. A value of
         *          true indicates that the list is not complete and the NextContinuationToken will be provided
         *          for a subsequent request.</p>
         */
        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        /**
         * <p>The marker that is used as a starting point for this analytics configuration list
         *          response. This value is present if it was sent in the request.</p>
         */
        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
            return this;
        }

        /**
         * <p>
         *             <code>NextContinuationToken</code> is sent when <code>isTruncated</code> is true, which
         *          indicates that there are more analytics configurations to list. The next request must
         *          include this <code>NextContinuationToken</code>. The token is obfuscated and is not a
         *          usable value.</p>
         */
        public final Builder nextContinuationToken(String nextContinuationToken) {
            this.nextContinuationToken = nextContinuationToken;
            return this;
        }

        /**
         * <p>The list of analytics configurations for a bucket.</p>
         */
        public final Builder analyticsConfigurationList(
                List<AnalyticsConfiguration> analyticsConfigurationList) {
            this.analyticsConfigurationList = analyticsConfigurationList;
            return this;
        }
    }
}
